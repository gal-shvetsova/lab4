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
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class GameControllerImpl implements GameController {
    private static final Logger logger = Logger.getLogger(String.valueOf(GameControllerImpl.class));
    static boolean initialized = false;

    private final View snakeGUI;
    private final String name;
    private final InetAddress inetAddress;
    private int port;
    private Game game;
    private ConcurrentMap<Pair<InetAddress, Integer>, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new ConcurrentHashMap<>();  //запущенные игры
    private Timer timer = new Timer();
    private final TimerTask messageSenderTask;
    private final GUIGameUpdater guiUpdater = new GUIGameUpdater();
    private GUIServersUpdater guiServersUpdater;
    private static GameController gameController = null;

    private GameControllerImpl(int port,
                               String name,
                               InetAddress inetAddress,
                               View snakeGUI) {
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.snakeGUI = snakeGUI;
        this.game = null;
        ProtoMessagesListenerImpl.getInstance();
        timer = new Timer();
        guiServersUpdater = new GUIServersUpdater();
        timer.schedule(guiServersUpdater, 100, 3000);
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
    }


    public static void init(String name, InetAddress inetAddress, int port, View snakeGui) {
        if (gameController == null) {
            initialized = true;
            gameController = new GameControllerImpl(port, name, inetAddress, snakeGui);
        }
    }

    public static GameController getInstance() {
        if (!initialized) {
            throw new RuntimeException("GameController's not initialized");
        }
        return gameController;
    }

    @Override
    public void addAvailableGame(InetAddress inetAddress, int port, SnakesProto.GameMessage.AnnouncementMsg message) {
        availableServers.put(ImmutablePair.of(inetAddress, port), message);
    }


    @Override
    public void addAliveGamer(InetAddress inetAddress, int port) {

    }

    @Override
    synchronized public void hostGame(String name, InetAddress address, int port) {
        logger.info("host " + address.toString() + " " + port);
        if (game == null) return;
        Optional<Gamer> gamerByAddress = game.getGamerByAddress(inetAddress, this.port);
        Gamer gamer = gamerByAddress.orElseThrow(() -> new NoSuchElementException("no gamers"));
        if (!gamer.isMaster()) return;
        Gamer newGamer = new Gamer(name, address, port, game.getGameConfig(), Role.NORMAL);
        newGamer.start();
        game.addGamer(newGamer);
        if (!game.hasDeputy()) {
            newGamer.setRole(Role.DEPUTY);
            Message message = new Message(MessageCreator.makeRoleChangeMessage(Role.DEPUTY), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message);
        }
        loadNewState();
    }

    @Override
    synchronized public void setGame(Game game) {
        if (this.game == null) {
            this.game = game;
            return;
        }

        if (getCurrentGamer().isEmpty() || getCurrentGamer().get().isMaster()) return;
        this.game = game;
    }

    @Override
    synchronized public void loadNewState() {
        if (!game.hasAliveGamers()) return;
        if (game.getGamerByAddress(inetAddress, port).isEmpty()) {
            endGame();
            return;
        }
        if (game.getGamerByAddress(inetAddress, port).get().isMaster()) return;
        if (!snakeGUI.isStarted())
            snakeGUI.startGame(game.getGameConfig());
        snakeGUI.loadNewField(game.makeRepresentation());
    }

    @Override
    synchronized public void changeSnakeDirection(InetAddress inetAddress, int port, Direction direction) {
        game.getGamerByAddress(inetAddress, port).get().moveSnake(direction);
    }

    @Override
    synchronized public void becomeMaster() {
        game.getGamerByAddress(inetAddress, port).get().setRole(Role.MASTER);
        timer.schedule(guiUpdater, 100, 100);
        timer.schedule(messageSenderTask, 0, 1000);
    }

    @Override
    public void becomeDeputy() {
//        game.getGamerByAddress(inetAddress, port).ifPresent(value -> value.setRole(Role.DEPUTY));
    }

    @Override
    synchronized public void joinGame(String addressStr, int port) {
        try {
            InetAddress inetAddress = InetAddress.getByName(addressStr);
            Message message = new Message(MessageCreator.makeJoinMsg(name), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message);

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
            game.getZombies().forEach(GameControllerImpl.this::endGame);
            game.removeZombies();
            for (Gamer gamer : game.getAliveGamers()) {
                if (!gamer.isViewer()) {
                    gamer.makeStep();
                }

                if (gamer.isMaster()) {
                    snakeGUI.loadNewField(game.makeMasterRepresentation());
                }

                if (!game.hasAliveGamers()) {
                    if (!game.hasDeputy() && game.getAliveGamers().size() > 1) {
                        SnakesProto.GameMessage roleMsg = MessageCreator.makeRoleChangeMessage(Role.DEPUTY);
                        Optional<Gamer> deputy = game.getAliveGamers().stream().filter(gamer1 -> !gamer1.isMaster() && !gamer1.isViewer()).findFirst();
                        if (deputy.isEmpty()) return;
                        deputy.get().setRole(Role.DEPUTY);
                        Message roleMessage = new Message(roleMsg, deputy.get().getIpAddress(), deputy.get().getPort());
                        MessageControllerImpl.getInstance().sendMessage(roleMessage);
                    }
                }
                Message message = new Message(MessageCreator.makeStateMessage(game), gamer.getIpAddress(), gamer.getPort());
                MessageControllerImpl.getInstance().sendMessage(message);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void startNewGame(GameConfig gameConfig) throws IllegalArgumentException {
        Gamer gamer = new Gamer(name, inetAddress, port, gameConfig, Role.MASTER);
        gamer.start();
        game = new Game(gameConfig);
        game.addGamer(gamer);

        timer.schedule(guiUpdater, 100, 100);
        timer.schedule(messageSenderTask, 0, 1000);

        snakeGUI.startGame(gameConfig);
    }


    public void endGame() {
        logger.info("end game");
        game = null;
        snakeGUI.endGame();
        guiUpdater.cancel();
        messageSenderTask.cancel();
    }

    public void endGame(Gamer g) {
        logger.info("end game g");
        if (g.getPort() == port && g.getIpAddress() == inetAddress) {
            endGame();
            Optional<Gamer> deputy = game.getDeputy();
            logger.info("empty " + deputy.isEmpty());
            if (deputy.isEmpty()) return;
            SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.MASTER);
            Message message = new Message(msg, deputy.get().getIpAddress(), deputy.get().getPort());
            MessageControllerImpl.getInstance().sendMessage(message);
        }
        //else send msg
    }

    public void keyActivity(int x, int y) {  //TODO: rename
        if (game == null) return;
        if (getCurrentGamer().isEmpty()) return;

        if (getCurrentGamer().get().isMaster()) {
            getCurrentGamer().get().moveSnake(Direction.getDirection(x, y));
        } else {
            SnakesProto.GameMessage protoMsg = MessageCreator.makeSteerMsg(Direction.getDirection(x, y));
            Gamer master = game.getMaster();
            Message message = new Message(protoMsg, master.getIpAddress(), master.getPort());
            MessageControllerImpl.getInstance().sendMessage(message);
        }
    }

    private Optional<Gamer> getCurrentGamer() {
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

