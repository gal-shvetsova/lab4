package fit.networks.controller;

import com.google.common.collect.Streams;
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
import java.util.Optional;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
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
    private Optional<Game> game;
    private ConcurrentMap<Pair<InetAddress, Integer>, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new ConcurrentHashMap<>();  //запущенные игры
    private Timer timer;
    private TimerTask messageSenderTask;
    private TimerTask remover = new RemoveGamersTask();
    private GUIGameUpdater guiUpdater = new GUIGameUpdater();
    private static GameController gameController = null;

    private GameControllerImpl(int port,
                               String name,
                               InetAddress inetAddress,
                               View snakeGUI) {
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.snakeGUI = snakeGUI;
        this.game = Optional.empty();
        ProtoMessagesListenerImpl.subscribe();
        timer = new Timer();
        timer.schedule(new GUIServersUpdater(), 100, 3000);
        messageSenderTask = new SenderTask();
    }

    private class RemoveGamersTask extends TimerTask {
        @Override
        public void run() {
            game.ifPresent(Game::makeZombiesFromInactiveGamers);
        }
    }

    private class SenderTask extends TimerTask {
        @Override
        public void run() {
            MessageController instance = MessageControllerImpl.getInstance();
            getCurrentGamer().ifPresent(gamer -> {
                try {
                    if (gamer.isMaster()) {
                        InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                        int port = Protocol.getMulticastPort();
                        SnakesProto.GameMessage protoAnnouncementMessage = MessageCreator.makeAnnouncementMessage(game.orElse(null));
                        Message announcementMessage = new Message(protoAnnouncementMessage, inetAddress, port);
                        instance.sendMessage(announcementMessage, false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            game.flatMap(Game::getMaster).ifPresent(gamer -> {
                SnakesProto.GameMessage protoMessage = MessageCreator.makePingMessage();
                InetAddress inetAddress = gamer.getIpAddress();
                int port = gamer.getPort();
                Message message = new Message(protoMessage, inetAddress, port);
                instance.sendMessage(message, false);
            });
        }
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
        getCurrentGamer().ifPresent(gamer -> {
            if (gamer.isMaster()) {
                game.ifPresent(game -> game.addAliveGamer(inetAddress, port));
            }
        });
    }

    @Override
    synchronized public void hostGame(String name, InetAddress address, int port) {
        game.ifPresent(game -> getCurrentGamer().ifPresent(gamer -> {
            if (!gamer.isMaster()) return;
            Gamer newGamer = new Gamer(name, address, port, game.getGameConfig(), Role.NORMAL, null, null);
            newGamer.start();
            game.addGamer(newGamer);
            if (game.deputyAbsent()) {
                newGamer.setRole(Role.DEPUTY);
                Message message = new Message(MessageCreator.makeRoleChangeMessage(Role.DEPUTY,
                        newGamer.getId(), gamer.getId()), inetAddress, port);
                MessageControllerImpl.getInstance().sendMessage(message, true);
            }
            loadNewState();
        }));

    }

    @Override
    synchronized public void setGame(Game game) {
        if (this.game.isEmpty() || getCurrentGamer().isEmpty()) {
            this.game = Optional.of(game);
            return;
        }

        if (getCurrentGamer().get().isMaster()) {
            return;
        }

        this.game = Optional.of(game);
        if (getCurrentGamer().isEmpty()) {
            endGame();
        }
    }

    @Override
    synchronized public void loadNewState() {
        game.ifPresent(game -> {
            Optional<Gamer> gamerByAddress = game.getGamerByAddress(inetAddress, port);
            if (gamerByAddress.isEmpty() || gamerByAddress.get().isMaster() || !game.hasAliveGamers()) {
                return;
            }

            if (!snakeGUI.isStarted()) {
                snakeGUI.startGame(game.getGameConfig());
            }
            snakeGUI.loadNewField(game.makeRepresentation());
        });
    }

    @Override
    synchronized public void changeSnakeDirection(InetAddress inetAddress, int port, Direction direction) {
        game.flatMap(game -> game.getGamerByAddress(inetAddress, port)).ifPresent(gamer -> gamer.moveSnake(direction));
    }

    @Override
    synchronized public void becomeMaster() {
        getCurrentGamer().ifPresent(gamer -> gamer.setRole(Role.MASTER));
        game.ifPresent(Game::removeDead);
        scheduleTasks();
    }

    @Override
    public void becomeDeputy() {
//        game.getGamerByAddress(inetAddress, port).ifPresent(value -> value.setRole(Role.DEPUTY));
    }

    @Override
    public Optional<Game> getGame() {
        return game;
    }

    @Override
    public void requestViewing() {
        getCurrentGamer().ifPresent(gamer -> {
            if (gamer.isMaster()) {
                gamer.setRole(Role.VIEWER);
                gamer.getSnake().becomeZombie();
                game.flatMap(Game::getDeputy).ifPresent(deputy -> {
                    SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.MASTER,
                            deputy.getId(), deputy.getId());
                    Message message = new Message(msg, deputy.getIpAddress(), deputy.getPort());
                    MessageControllerImpl.getInstance().sendMessage(message, true);
                });
            } else {
                game.flatMap(Game::getMaster).ifPresent(master -> {
                    SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.VIEWER,
                            master.getId(), gamer.getId());
                    Message message = new Message(msg, master.getIpAddress(), master.getPort());
                    MessageControllerImpl.getInstance().sendMessage(message, true);
                });

            }
        });
    }


    @Override
    public void becomeViewer(InetAddress inetAddress, int port) {
        game.flatMap(game -> game.getGamerByAddress(inetAddress, port)).ifPresent(gamer -> {
            gamer.setRole(Role.VIEWER);
            gamer.getSnake().becomeZombie();
        });
    }

    @Override
    synchronized public void joinGame(String addressStr, int port) {
        try {
            logger.info("join game");
            InetAddress inetAddress = InetAddress.getByName(addressStr);
            Message message = new Message(MessageCreator.makeJoinMsg(name), inetAddress, port);
            MessageControllerImpl.getInstance().sendMessage(message, true);
            timer = new Timer();
            timer.schedule(messageSenderTask, 0, 2000);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class GUIServersUpdater extends TimerTask {
        synchronized private String[][] makeAvailableServersTable() {
            String[][] table = new String[availableServers.size()][4];
            int i = 0;
            for (SnakesProto.GameMessage.AnnouncementMsg msg : availableServers.values()) {
                SnakesProto.GamePlayer master = ProtoHelper.getMaster(msg.getPlayers());
                if (master == null) {
                    continue;
                }
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
            if (game.isEmpty() || game.get().getAliveGamers().isEmpty()) {
                return null;
            }

            Game currentGame = game.get();

            String[][] table = new String[currentGame.getAliveGamers().size()][3];
            int i = 0;
            for (Gamer gamer : currentGame.getSorted()) {
                if (i >= table.length) {
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
            if (game.isEmpty()) {
                return;
            }

            for (Gamer gamer : game.get().getAliveGamers()) {
                gamer.makeStep();

                if (gamer.isMaster()) {
                    game.get().getDying().forEach(g -> endGame(g));
                    if (game.isEmpty()) {
                        return;
                    }
                    snakeGUI.loadNewField(game.get().makeMasterRepresentation());
                }

                Game currentGame = game.get();
                if (currentGame.getAliveGamers().size() > 1) {
                    currentGame.getDeputy().ifPresent(deputy -> getCurrentGamer().ifPresent(currentGamer -> {
                        SnakesProto.GameMessage roleMsg = MessageCreator
                                .makeRoleChangeMessage(Role.DEPUTY, deputy.getId(), currentGamer.getId());
                        deputy.setRole(Role.DEPUTY);
                        Message roleMessage = new Message(roleMsg, deputy.getIpAddress(), deputy.getPort());
                        MessageControllerImpl.getInstance().sendMessage(roleMessage, true);
                    }));
                }
                Message message = new Message(MessageCreator.makeStateMessage(currentGame), gamer.getIpAddress(), gamer.getPort());
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
        Game newGame = new Game(gameConfig);
        newGame.addGamer(gamer);
        game = Optional.of(newGame);
        scheduleTasks();
        snakeGUI.startGame(gameConfig);
    }

    private void scheduleTasks() {
        game.ifPresent(game -> {
            guiUpdater = new GUIGameUpdater();
            messageSenderTask = new SenderTask();
            remover = new RemoveGamersTask();
            timer.schedule(guiUpdater, 100, game.getGameConfig().getDelayMs());
            timer.schedule(messageSenderTask, 0, 1000);
            timer.schedule(remover, 3000, 3000);
        });
    }


    synchronized public void endGame() {
        game.ifPresent(game -> {
            game.removeDead();
            snakeGUI.endGame();
            guiUpdater.cancel();
            remover.cancel();
            messageSenderTask.cancel();
        });
        game = Optional.empty();

    }

    synchronized public void endGame(Gamer gamer) {
        game.ifPresent(game -> getCurrentGamer().ifPresent(currentGamer -> {
            final boolean isCurrent = gamer.equals(getCurrentGamer().get());
            if (currentGamer.isMaster()) {
                Queue<Gamer> deadGamers = game.getDead();
                game.removeDead();

                SnakesProto.GameMessage stateMessage = MessageCreator.makeStateMessage(game);

                Streams.concat(game.getAliveGamers().stream(), deadGamers.stream())
                        .forEach(g -> {
                            Message msgState = new Message(stateMessage, g.getIpAddress(), g.getPort());
                            MessageControllerImpl.getInstance().sendMessage(msgState, false);
                        });

                game.getDeputy().ifPresent(deputy -> {
                    if (isCurrent) {
                        SnakesProto.GameMessage msg = MessageCreator.makeRoleChangeMessage(Role.MASTER,
                                deputy.getId(), currentGamer.getId());
                        Message message = new Message(msg, deputy.getIpAddress(), deputy.getPort());
                        MessageControllerImpl.getInstance().sendMessage(message, true);
                    }
                });
            }
            if (isCurrent) {
                endGame();
            }
        }));

    }

    public void keyActivity(int x, int y) {
        game.ifPresent(game -> getCurrentGamer().ifPresent(gamer -> {
            if (gamer.isViewer()) {
                return;
            }
            if (gamer.isMaster()) {
                gamer.moveSnake(Direction.getDirection(x, y));
            } else {
                game.getMaster().ifPresent(master -> {
                    SnakesProto.GameMessage protoMsg = MessageCreator.makeSteerMsg(Direction.getDirection(x, y));
                    Message message = new Message(protoMsg, master.getIpAddress(), master.getPort());
                    MessageControllerImpl.getInstance().sendMessage(message, true);
                });
            }
        }));
    }

    private Optional<Gamer> getCurrentGamer() {
        if (game.isEmpty()) {
            return Optional.empty();
        }
        return game.get().getGamerByAddress(inetAddress, port);
    }

    public void start() {
        snakeGUI.showForm();
    }
}

