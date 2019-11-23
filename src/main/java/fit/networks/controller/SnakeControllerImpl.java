package fit.networks.controller;

import fit.networks.game.GameConfig;
import fit.networks.gamer.Gamer;
import fit.networks.gui.SnakeGUI;
import fit.networks.protocol.ProtoHelper;
import fit.networks.protocol.SnakesProto;
import fit.networks.view.View;

import java.net.InetAddress;
import java.util.*;

public class SnakeControllerImpl implements SnakeController {
    private View snakeGUI;
    private Gamer gamer;
    private String name;
    private InetAddress inetAddress;
    private int port;
    private Map<Integer, SnakesProto.GameMessage.AnnouncementMsg> availableServers = new HashMap<>();  //запущенные игры
    private Timer timer = new Timer();
    private MultiController multiController;
    private Thread messageReceiver;
    private TimerTask messageSender;
    private GUIUpdater guiUpdater;
    private static SnakeController snakeController = null;

    private SnakeControllerImpl(String name, InetAddress inetAddress, int port) {
        this.snakeGUI = new SnakeGUI();
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.gamer = null;
        this.multiController = new MultiControllerImpl();
        this.messageReceiver = new Thread(() -> {
            while (true) {
                SnakesProto.GameMessage message = multiController.receiveMessage();
                if (message == null) continue;
                switch (message.getTypeCase()) {
                    case ANNOUNCEMENT:
                        SnakesProto.GamePlayer master = ProtoHelper.getMasterId(message.getAnnouncement().getPlayers());
                        if (master == null) {
                            break;
                        }
                        availableServers.put(master.getId(), message.getAnnouncement());
                        break;
                    case PING:
                        break;
                }
            }
        });
        this.messageReceiver.start();
    }

    public static SnakeController getController(String name, InetAddress inetAddress, int port){
        if (snakeController == null){
            snakeController = new SnakeControllerImpl(name, inetAddress, port);
        }

        return snakeController;
    }

    public static SnakeController getController(){
        return snakeController;
    }


    private class GUIUpdater extends TimerTask {
        private String[][] makeAvailableServersTable() {
            String[][] table = new String[availableServers.size()][4];
            int i = 0;
            for (SnakesProto.GameMessage.AnnouncementMsg msg : availableServers.values()) {
                for (SnakesProto.GamePlayer player : msg.getPlayers().getPlayersList()) {
                    if (player.getRole() == SnakesProto.NodeRole.MASTER) {
                        table[i][0] = player.getName() + " [" + player.getIpAddress() + "]";
                        break;
                    }
                }
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
        timer.schedule(guiUpdater, 0, 100);
        messageSender = new TimerTask() {
            @Override
            public void run() {
                multiController.sendMessage(MessageBuilder.makeAnnouncementMessage(gamer.getGame()));
            }
        };
        timer.schedule(messageSender, 0, 1000);
        snakeGUI.startGame(gameConfig);
    }

    public void joinGame() {

    }

    public void endGame() {
        gamer = null;
        snakeGUI.endGame();
        messageReceiver.interrupt();
        guiUpdater.cancel();
        messageSender.cancel();
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

