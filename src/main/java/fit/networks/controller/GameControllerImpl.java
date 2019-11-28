package fit.networks.controller;

import fit.networks.game.GameConfig;
import fit.networks.gamer.Gamer;
import fit.networks.gui.SnakeGUI;
import fit.networks.protocol.ProtoHelper;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;
import fit.networks.view.View;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameControllerImpl implements GameController {
    private View snakeGUI;
    private Gamer gamer;
    private String name;
    private InetAddress inetAddress;
    private int port;
    //TODO: delete identical game
    private ConcurrentMap<Pair<InetAddress, Integer>, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new ConcurrentHashMap<>();  //запущенные игры
    private Timer timer = new Timer();
    private TimerTask messageSenderTask;
    private GUIUpdater guiUpdater;
    private static GameController snakeController = null;

    private GameControllerImpl(String name, InetAddress inetAddress, int port) {
        this.snakeGUI = new SnakeGUI();
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.gamer = null;
        MessageControllerImpl.startMessageController(inetAddress, port);
        ProtoMessagesListenerImpl.getListener();
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
    public void joinGame(String addressStr, int parseInt) {

    }

    private class GUIUpdater extends TimerTask {
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
            if (gamer == null) return;
            if (gamer.isZombie()) {
                snakeGUI.showDeadForm();
            } else
                gamer.makeStep();
            snakeGUI.loadNewField(gamer.getRepresentation());
            if (!gamer.getGame().hasAliveGamers()) {
                endGame();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void startNewGame(GameConfig gameConfig) throws IllegalArgumentException {
        gamer = Gamer.getNewGameMaster(name, inetAddress, port, gameConfig);
        gamer.startNewGame();
        timer = new Timer();
        guiUpdater = new GUIUpdater();
        timer.schedule(guiUpdater, 100, 100);
        messageSenderTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                    int port = Protocol.getMulticastPort();
                    SnakesProto.GameMessage protoAnnouncementMessage = MessageBuilder.makeAnnouncementMessage(gamer.getGame());
                    Message announcementMessage = new Message(protoAnnouncementMessage, inetAddress, port);
                    SnakesProto.GameMessage protoStateMessage = MessageBuilder.makeStateMessage(gamer);
                    Message stateMessage = new Message(protoStateMessage, inetAddress, port);
                    MessageControllerImpl.getMessageController().sendMessage(announcementMessage);
                    MessageControllerImpl.getMessageController().sendMessage(stateMessage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        timer.schedule(messageSenderTask, 0, 1000);
        snakeGUI.startGame(gameConfig);
    }

    public void joinGame() {

    }

    public void endGame() {
        gamer = null;
        snakeGUI.endGame();
        guiUpdater.cancel();
        messageSenderTask.cancel();
    }

    public void keyActivity(int x, int y) {  //TODO: rename
        SnakesProto.GameMessage.SteerMsg.Builder steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        gamer.moveSnake(x, y);
    }

    public void start() {
        snakeGUI.showForm();
        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void leaveGame() {

    }

}

