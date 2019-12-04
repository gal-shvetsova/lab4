package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.gamer.Gamer;
import fit.networks.gamer.Role;
import fit.networks.protocol.ProtoHelper;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;
import fit.networks.view.View;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

public class GameControllerImpl implements GameController {
    private static final Logger logger = Logger.getLogger(String.valueOf(GameControllerImpl.class));

    private final View snakeGUI;
    private String name;
    private InetAddress inetAddress;
    private int port;
    private Game game;
    private ConcurrentMap<Pair<InetAddress, Integer>, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new ConcurrentHashMap<>();  //запущенные игры
    private Timer timer = new Timer();
    private TimerTask messageSenderTask;
    private GUIGameUpdater guiUpdater;
    private GUIServersUpdater guiServersUpdater;
    private static GameController snakeController = null;

    private final MessageController messageController = MessageControllerImpl.getInstance();

    private GameControllerImpl(int port, String name, InetAddress inetAddress, View snakeGUI) {
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.snakeGUI = snakeGUI;
        this.game = null;
        MessageControllerImpl.startMessageController(inetAddress, port);
        ProtoMessagesListenerImpl.getListener();
        timer = new Timer();
        guiServersUpdater = new GUIServersUpdater();
        timer.schedule(guiServersUpdater, 100, 3000);
    }

    public static GameController getController(String name, InetAddress inetAddress, int port, View snakeGui) {
        if (snakeController == null) {
            snakeController = new GameControllerImpl(port, name, inetAddress, snakeGui);
        }
        return snakeController;
    }

    public static GameController getController() {
        return snakeController;
    }

    @Override
    public void addAvailableGame(InetAddress inetAddress, int port, SnakesProto.GameMessage.AnnouncementMsg message) {
        System.out.println(availableServers.containsKey(ImmutablePair.of(inetAddress, port)));
        availableServers.put(ImmutablePair.of(inetAddress, port), message);
    }


    @Override
    public void addAliveGamer(InetAddress inetAddress, int port) {

    }

    @Override
    synchronized public void hostGame(String name, InetAddress address, int port) {
        Logger logger = Logger.getLogger("gamer");
        logger.info("host");
        if (game == null) return;
        Optional<Gamer> gamerByAddress = game.getGamerByAddress(inetAddress, this.port);
        Gamer gamer = gamerByAddress.orElseThrow(() -> new NoSuchElementException("no gamers"));
        if(!gamer.isMaster()) return;
        game.addGamer(new Gamer(name, address, port, game.getGameConfig(), Role.NORMAL));
        if (!game.hasDeputy()) {
            Message message = new Message(MessageCreator.makeRoleChangeMessage(Role.DEPUTY), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message);
        }
     //   logger.info(game.getActiveGamers().size() + " size ");
    }

    @Override
    synchronized public void setGame(Game game) {
        if (this.game == null) {
            this.game = game;
            return;
        }
        if (getCurrentGamer() == null || getCurrentGamer().isMaster()) return;
  //      logger.info(game.getActiveGamers().size() + " ");
        this.game = game;
    }

    @Override
    synchronized public void loadNewState() {
   //     logger.info(snakeGUI.isStarted() + " ");
        if (!snakeGUI.isStarted())
            snakeGUI.startGame(game.getGameConfig());
        snakeGUI.loadNewField(game.makeMasterRepresentation());
    }

    @Override
    synchronized public void changeSnakeDirection(InetAddress inetAddress, int port, Direction direction) {
        game.getGamerByAddress(inetAddress, port).get().moveSnake(direction);
    }

    @Override
    synchronized public void becomeMaster() {
        game.getGamerByAddress(inetAddress, port).get().setRole(Role.MASTER);
    }

    @Override
    synchronized public void joinGame(String addressStr, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(addressStr);
            Message message = new Message(MessageCreator.makeJoinMsg(name), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message);
            logger.info("send join " + inetAddress + " " + port);

        } catch (IOException ex) {
            ex.printStackTrace(); //TODO: gui loads inet address and port as int
        }
    }

    private class GUIServersUpdater extends TimerTask {
        synchronized private String[][] makeAvailableServersTable() {
            String[][] table = new String[availableServers.size()][4];
            int i = 0;
            for (SnakesProto.GameMessage.AnnouncementMsg msg : availableServers.values()) {
                SnakesProto.GamePlayer master = ProtoHelper.getMaster(msg.getPlayers());
                if (master == null) continue;
                table[i][0] = master.getName() + " [" + master.getIpAddress() + "]" + " [" + master.getPort() + "]";
                table[i][1] = ((Integer) msg.getPlayers().getPlayersList().size()).toString();
                table[i][2] = msg.getConfig().getWidth() + " * " + msg.getConfig().getHeight();
                table[i][3] = msg.getConfig().getFoodStatic() + " + " + msg.getConfig().getFoodPerPlayer() + "x";
                i++;
            }
            return table;
        }

        @Override
        public void run() {
            snakeGUI.loadAvailableGames(makeAvailableServersTable());
        }
    }

    private class GUIGameUpdater extends TimerTask {
        @Override
        synchronized public void run() {
            game.removeZombies();
            for (Gamer gamer : game.getAliveGamers()) {
                if (gamer == null) return;
                if (gamer.getSnake() == null) return;
                if (!gamer.isViewer()) {
                    gamer.makeStep();
                }

                SnakesProto.GameMessage protoMsg = MessageCreator.makeStateMessage(game);
                Message message = new Message(protoMsg, gamer.getIpAddress(), gamer.getPort());
                MessageControllerImpl.getInstance().sendMessage(message);

                if (gamer.isMaster())
                    snakeGUI.loadNewField(game.makeMasterRepresentation());
                else
                    snakeGUI.loadNewField(game.makeRepresentation());
                if (!game.hasAliveGamers()) {
                    if (!game.hasDeputy()) {
                        SnakesProto.GameMessage roleMsg = MessageCreator.makeRoleChangeMessage(Role.MASTER);
                        Gamer deputy = game.getDeputy();
                        if (deputy == null) return;
                        Message roleMessage = new Message(roleMsg, deputy.getIpAddress(), deputy.getPort());
                        MessageControllerImpl.getInstance().sendMessage(roleMessage);
                    }
                    endGame();
                }
            }
        }

    }

    public String getName() {
        return name;
    }

    public void startNewGame(GameConfig gameConfig) throws IllegalArgumentException {
        Gamer gamer = Gamer.getNewGameMaster(name, inetAddress, port, gameConfig);
        game = new Game(gameConfig);
        game.addGamer(gamer);
        gamer.getSnake().randomStart();

        guiUpdater = new GUIGameUpdater();
        timer.schedule(guiUpdater, 100, 100);

        messageSenderTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                    int port = Protocol.getMulticastPort();
                    SnakesProto.GameMessage protoAnnouncementMessage = MessageCreator.makeAnnouncementMessage(game);
                    Message announcementMessage = new Message(protoAnnouncementMessage, inetAddress, port);
                    MessageControllerImpl.getInstance().sendMessage(announcementMessage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        timer.schedule(messageSenderTask, 0, 1000);
        snakeGUI.startGame(gameConfig);
    }


    public void endGame() {
        game = null;
        snakeGUI.endGame();
        guiUpdater.cancel();
        messageSenderTask.cancel();
    }

    public void keyActivity(int x, int y) {  //TODO: rename
        if (game == null) return;

        if (getCurrentGamer().isMaster()) {
            getCurrentGamer().moveSnake(Direction.getDirection(x, y));
        } else {
            SnakesProto.GameMessage protoMsg = MessageCreator.makeSteerMsg(Direction.getDirection(x, y));
            Gamer master = game.getMaster();
            Message message = new Message(protoMsg, master.getIpAddress(), master.getPort());
            MessageControllerImpl.getInstance().sendMessage(message);
        }
    }

    private Gamer getCurrentGamer() {
        return game.getGamerByAddress(inetAddress, port);
    }

    public void start() {
        snakeGUI.showForm();
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void leaveGame() {
        game = null;
        messageSenderTask.cancel();
        guiUpdater.cancel();
        snakeGUI.endGame();
    }

}

