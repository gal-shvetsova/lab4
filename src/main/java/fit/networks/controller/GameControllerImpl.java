package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.gamer.Gamer;
import fit.networks.gamer.Role;
import fit.networks.gui.SnakeGUI;
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
    private View snakeGUI;
    private String name;
    private InetAddress inetAddress;
    private int port;
    private Game game;
    Logger logger = Logger.getLogger("controller");
    //TODO: delete identical game
    private ConcurrentMap<Pair<InetAddress, Integer>, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new ConcurrentHashMap<>();  //запущенные игры
    private Timer timer = new Timer();
    private TimerTask messageSenderTask;
    private GUIGameUpdater guiUpdater;
    private GUIServersUpdater guiServersUpdater;
    private static GameController snakeController = null;

    private GameControllerImpl(String name, InetAddress inetAddress, int port) {
        this.snakeGUI = new SnakeGUI();
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.game = null;
        MessageControllerImpl.startMessageController(inetAddress, port);
        ProtoMessagesListenerImpl.getListener();
        timer = new Timer();
        guiServersUpdater = new GUIServersUpdater();
        timer.schedule(guiServersUpdater, 100, 3000);
    }

    public static GameController getController(String name, InetAddress inetAddress, int port) {
        if (snakeController == null) {
            snakeController = new GameControllerImpl(name, inetAddress, port);
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
        if (!game.getGamerByAddress(inetAddress, this.port).isMaster()) return; //TODO: exception

        Gamer newGamer = new Gamer(name, address, port, game.getGameConfig(), Role.NORMAL);
        newGamer.getSnake().randomStart();
        game.addGamer(newGamer);
        logger.info(game.getActiveGamers().size() + " size ");
    }

    @Override
    synchronized public void setGame(Game game) {
        if (this.game == null) {
            this.game = game;
            return;
        }
        if (getCurrentGamer() == null || getCurrentGamer().isMaster()) return;
        logger.info(game.getActiveGamers().size() + " ");
        this.game = game;
    }

    @Override
    synchronized public void loadNewState() {
        logger.info(snakeGUI.isStarted() + " ");
        if (!snakeGUI.isStarted())
            snakeGUI.startGame(game.getGameConfig());
        snakeGUI.loadNewField(game.makeRepresentation());
    }

    @Override
    synchronized public void joinGame(String addressStr, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(addressStr);
            Message message = new Message(MessageCreator.makeJoinMsg(name), inetAddress, port);
            MessageControllerImpl.getMessageController().sendMessage(message);
            logger.info("send join " + inetAddress + " " + port);

        } catch (IOException ex) {
            ex.printStackTrace(); //TODO: gui loads inet address and port as int
        }
    }

    private class GUIServersUpdater extends TimerTask {
        private String[][] makeAvailableServersTable() {
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
        public void run() {
          //  logger.info(game.getActiveGamers().size() + " size ");
            for (Gamer gamer : game.getActiveGamers()) {
            //    logger.info(gamer.getSnake().getKeyPoints().toString());
                if (gamer == null) return;
                if (gamer.getSnake() == null) return;
                if (gamer.isZombie()) {
                    snakeGUI.showDeadForm();
                } else
                    gamer.makeStep();
             //   Logger logger = Logger.getLogger("controller");
              //  logger.info("size = " + game.getActiveGamers().size());
                SnakesProto.GameMessage protoMsg = MessageCreator.makeStateMessage(game);
                Message message = new Message(protoMsg, gamer.getIpAddress(), gamer.getPort());
                MessageControllerImpl.getMessageController().sendMessage(message);


                snakeGUI.loadNewField(game.makeRepresentation());
                if (!game.hasAliveGamers()) {
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
                    MessageControllerImpl.getMessageController().sendMessage(announcementMessage);
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
        SnakesProto.GameMessage.SteerMsg.Builder steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        if (getCurrentGamer() == null) return;
        if (getCurrentGamer().isMaster()) {
            getCurrentGamer().moveSnake(x, y);
        }
    }

    private Gamer getCurrentGamer() {
        if (game == null) return null;
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

