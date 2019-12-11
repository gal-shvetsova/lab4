package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.game.snake.State;
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
    static boolean initialized = false;

    private final View snakeGUI;
    private final String name;
    private final InetAddress inetAddress;
    private int port;
    private Game game;
    private ConcurrentMap<Pair<InetAddress, Integer>, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new ConcurrentHashMap<>();  //запущенные игры
    private Timer timer = new Timer();
    private TimerTask messageSenderTask;
    private TimerTask remover = new RemoveGamersTask();
    private GUIGameUpdater guiUpdater = new GUIGameUpdater();
    private final GUIServersUpdater guiServersUpdater = new GUIServersUpdater();
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
        timer.schedule(guiServersUpdater, 100, 3000);
        messageSenderTask = new SenderTask();
    }

    private class RemoveGamersTask extends TimerTask {
        @Override
        public void run() {
            game.makeZombiesFromInactiveGamers();
        }
    }

    private class SenderTask extends TimerTask {
        @Override
        public void run() {
            try {
                if (getCurrentGamer().isEmpty()) {
                    return;
                }
                MessageController instance = MessageControllerImpl.getInstance();

                if (getCurrentGamer().get().isMaster()) {
                    InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                    int port = Protocol.getMulticastPort();
                    SnakesProto.GameMessage protoAnnouncementMessage = MessageCreator.makeAnnouncementMessage(game);
                    Message announcementMessage = new Message(protoAnnouncementMessage, inetAddress, port);
                    instance.sendMessage(announcementMessage, false);
                }

                if (game.getMaster().isEmpty()) {
                    return;
                }

                SnakesProto.GameMessage protoMessage = MessageCreator.makePingMessage();
                InetAddress inetAddress = game.getMaster().get().getIpAddress();
                int port = game.getMaster().get().getPort();
                Message message = new Message(protoMessage, inetAddress, port);
                instance.sendMessage(message, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    ;


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
        if (getCurrentGamer().isPresent() && getCurrentGamer().get().isMaster()) {
            game.addAliveGamer(inetAddress, port);
        }
    }

    @Override
    synchronized public void hostGame(String name, InetAddress address, int port) {
        if (game == null) return;
        Optional<Gamer> currentGamer = getCurrentGamer();
        if (currentGamer.isEmpty()) {
            return;
        }
        Gamer gamer = currentGamer.get();
        if (!gamer.isMaster()) return;
        Gamer newGamer = new Gamer(name, address, port, game.getGameConfig(), Role.NORMAL, null, null);
        newGamer.start();
        game.addGamer(newGamer);
        if (game.deputyAbsent()) {
            newGamer.setRole(Role.DEPUTY);
            Message message = new Message(MessageCreator.makeRoleChangeMessage(Role.DEPUTY, newGamer.getId(), gamer.getId()), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message, true);
        }
        loadNewState();
    }

    @Override
    synchronized public void setGame(Game game) {
        if (this.game == null || getCurrentGamer().isEmpty()) {
            this.game = game;
            return;
        }

        if (getCurrentGamer().get().isMaster()) return;
        this.game = game;
        if (getCurrentGamer().isEmpty()) {
            endGame();
        }
    }

    @Override
    synchronized public void loadNewState() {
        if (game == null) return;
        if (game.getGamerByAddress(inetAddress, port).isEmpty()) {
            return;
        }
        if (game.getGamerByAddress(inetAddress, port).get().isMaster()) return;

        if (!game.hasAliveGamers()) return;
        if (game.getGamerByAddress(inetAddress, port).isEmpty()) {
            //endGame();
            return;
        }
        if (!snakeGUI.isStarted()) {
            snakeGUI.startGame(game.getGameConfig());
        }
        snakeGUI.loadNewField(game.makeRepresentation());
    }

    @Override
    synchronized public void changeSnakeDirection(InetAddress inetAddress, int port, Direction direction) {
        if (game != null && game.getGamerByAddress(inetAddress, port).isPresent()) {
            game.getGamerByAddress(inetAddress, port).get().moveSnake(direction);
        }
    }

    @Override
    synchronized public void becomeMaster() {
        logger.info("become master");

        Optional<Gamer> currentGamer = getCurrentGamer();
        if (currentGamer.isEmpty()) {
            return;
        }

        currentGamer.get().setRole(Role.MASTER);
        game.removeDead();

        guiUpdater = new GUIGameUpdater();
        messageSenderTask = new SenderTask();
        remover = new RemoveGamersTask();
        timer.schedule(guiUpdater, 100, 100);
        timer.schedule(messageSenderTask, 0, 1000);
        timer.schedule(remover, 3000, 3000);
    }

    @Override
    public void becomeDeputy() {
//        game.getGamerByAddress(inetAddress, port).ifPresent(value -> value.setRole(Role.DEPUTY));
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void requestViewing() {
        if (getCurrentGamer().isPresent() && getCurrentGamer().get().isMaster()) {
            getCurrentGamer().get().setRole(Role.VIEWER);
            getCurrentGamer().get().getSnake().setState(State.ZOMBIE);
            if (game.getDeputy().isEmpty()) {
                return;
            }

            Gamer deputy = game.getDeputy().get();

            int id = getCurrentGamer().get().getId();
            SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.MASTER, deputy.getId(), id);
            Message message = new Message(msg, deputy.getIpAddress(), deputy.getPort());
            MessageControllerImpl.getInstance().sendMessage(message, true);
        } else {
            if (game.getMaster().isEmpty()) {
                return;
            }
            Gamer master = game.getMaster().get();
            int id = getCurrentGamer().get().getId();
            SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.VIEWER, master.getId(), id);
            Message message = new Message(msg, master.getIpAddress(), master.getPort());
            MessageControllerImpl.getInstance().sendMessage(message, true);
        }
    }

    @Override
    public void becomeViewer(InetAddress inetAddress, int port) {
        if (game.getGamerByAddress(inetAddress, port).isEmpty()){
            return;
        }

        Gamer gamer = game.getGamerByAddress(inetAddress, port).get();
        gamer.setRole(Role.VIEWER);
        gamer.getSnake().setState(State.ZOMBIE);
    }

    @Override
    synchronized public void joinGame(String addressStr, int port) {
        try {
            logger.info("join game");
            InetAddress inetAddress = InetAddress.getByName(addressStr);
            Message message = new Message(MessageCreator.makeJoinMsg(name), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message, true);
            timer.schedule(messageSenderTask, 0, 2000);
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
            availableServers.clear();
            return table;
        }

        synchronized private String[][] makeRatingTable() {
            if (game == null || game.getAliveGamers().isEmpty()) {
                return null;
            }
            String[][] table = new String[game.getAliveGamers().size()][3];
            int i = 0;
            for (Gamer gamer : game.getSorted()) {
                if (i >= table.length){
                    break;
                }
                table[i][0] = i + "";
                table[i][1] = gamer.getName();
                table[i][2] = gamer.getScore() + "";
                i++;
            }
            return table;
        }

        @Override
        synchronized public void run() {
            snakeGUI.loadAvailableGames(makeAvailableServersTable());
            snakeGUI.loadRatingTable(makeRatingTable());
        }
    }

    private class GUIGameUpdater extends TimerTask {
        @Override
        synchronized public void run() {
            for (Gamer gamer : game.getAliveGamers()) {
                    gamer.makeStep();

                if (gamer.isMaster()) {
                    snakeGUI.loadNewField(game.makeMasterRepresentation());
                    game.getDying().forEach(g -> endGame(g));
                    if (game == null) return;
                }

                if (game.hasAliveGamers()) {
                    if (game.deputyAbsent() && game.getAliveGamers().size() > 1) {
                        Optional<Gamer> deputy = game.getAliveGamers().stream().filter(gamer1 -> !gamer1.isMaster() && !gamer1.isViewer()).findFirst();
                        Optional<Gamer> currentGamer = getCurrentGamer();
                        if (deputy.isPresent() && currentGamer.isPresent()) {
                            SnakesProto.GameMessage roleMsg = MessageCreator.makeRoleChangeMessage(Role.DEPUTY, deputy.get().getId(), currentGamer.get().getId());
                            deputy.get().setRole(Role.DEPUTY);
                            Message roleMessage = new Message(roleMsg, deputy.get().getIpAddress(), deputy.get().getPort());
                            MessageControllerImpl.getInstance().sendMessage(roleMessage, true);
                        }
                    }
                }
                Message message = new Message(MessageCreator.makeStateMessage(game), gamer.getIpAddress(), gamer.getPort());
                MessageControllerImpl.getInstance().sendMessage(message, false);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void startNewGame(GameConfig gameConfig) throws IllegalArgumentException {
        Gamer gamer = new Gamer(name, inetAddress, port, gameConfig, Role.MASTER, null, null);
        gamer.start();
        game = new Game(gameConfig);
        game.addGamer(gamer);
        guiUpdater = new GUIGameUpdater();
        messageSenderTask = new SenderTask();
        remover = new RemoveGamersTask();
        timer.schedule(guiUpdater, 100, 100);
        timer.schedule(messageSenderTask, 0, 1000);
        timer.schedule(remover, 3000, 3000);
        snakeGUI.startGame(gameConfig);
    }


    public void endGame() {
        logger.info("end game");
        game.removeDead();
        game = null;
        snakeGUI.endGame();
        guiUpdater.cancel();
        remover.cancel();
        messageSenderTask.cancel();
    }

    synchronized public void endGame(Gamer g) {
        logger.info("end game");
        boolean isCurrent = false;
        int senderId = g.getId();
        if (g.getPort() == port && g.getIpAddress().equals(inetAddress)) {
            logger.info("true");
            isCurrent = true;
        }

        if (game.getMaster().isPresent() && game.getMaster().get().equals(getCurrentGamer().get())) {
            logger.info("inside");
            Optional<Gamer> deputy = game.getDeputy();
            if (deputy.isEmpty()) return;

            Queue<Gamer> deadGamers = game.getDead();

            game.removeDead();

            SnakesProto.GameMessage stateMessage = MessageCreator.makeStateMessage(game);

            for (Gamer gamer : game.getAliveGamers()) {
                Message msgState = new Message(stateMessage, gamer.getIpAddress(), gamer.getPort());
                MessageControllerImpl.getInstance().sendMessage(msgState, false);
            }

            for (Gamer gamer : deadGamers) {
                Message msgState = new Message(stateMessage, gamer.getIpAddress(), gamer.getPort());
                MessageControllerImpl.getInstance().sendMessage(msgState, false);
            }

            if (isCurrent) {
                SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.MASTER, deputy.get().getId(), senderId);
                Message message = new Message(msg, deputy.get().getIpAddress(), deputy.get().getPort());
                MessageControllerImpl.getInstance().sendMessage(message, true);
            }

        }
        if (isCurrent) {
            endGame();
        }
    }

    public void keyActivity(int x, int y) {  //TODO: rename
        if (game == null) return;
        if (getCurrentGamer().isEmpty()) return;

        if (getCurrentGamer().get().isMaster()) {
            getCurrentGamer().get().moveSnake(Direction.getDirection(x, y));
        } else {
            if (getCurrentGamer().get().isViewer()){
                return;
            }
            SnakesProto.GameMessage protoMsg = MessageCreator.makeSteerMsg(Direction.getDirection(x, y));
            Optional<Gamer> master = game.getMaster();
            if (master.isEmpty()) {
                return;
            }
            Message message = new Message(protoMsg, master.get().getIpAddress(), master.get().getPort());
            MessageControllerImpl.getInstance().sendMessage(message, true);
        }
    }

    private Optional<Gamer> getCurrentGamer() {
        ;
        if (game == null) return Optional.empty();
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

