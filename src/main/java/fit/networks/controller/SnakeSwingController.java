package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.gamer.Gamer;
import fit.networks.gui.SnakeGUI;
import fit.networks.protocol.SnakesProto;

import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class SnakeSwingController extends SnakeController {
    private SnakeGUI snakeGUI;
    private Gamer gamer;
    private String name;
    private InetAddress inetAddress;
    private int port;
    private Set<Game> availableServers = new HashSet<>();  //запущенные игры
    private Timer timer = new Timer();
    private MessageManager messageManager;

    public SnakeSwingController(String name, InetAddress inetAddress, int port) {
        this.snakeGUI = new SnakeGUI(this);
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.gamer = null;
        this.messageManager = MessageManager.createMessageManager(this); //TODO remove this
    }


    private class GUIUpdater extends TimerTask {
        @Override
        public void run() {
            if (gamer == null)  return;
            if (gamer.isZombie()) {
                snakeGUI.showDeadForm();
            } else
                gamer.makeStep();
            snakeGUI.loadNewField(gamer.getRepresentation());
            if (!gamer.getGame().hasAliveGamers()){
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
        timer.schedule(new GUIUpdater(), 0, 100);
        messageManager.schedulePing();
        messageManager.scheduleAnnouncement(gamer);
    }

    public void joinGame(){

    }

    public void endGame(){
        messageManager.cancelPing();
        messageManager.cancelAnnouncement();

        gamer = null;
        snakeGUI.endGame();
        timer.cancel();
    }

    public void masterKeyPressed(int keyEvent){
        switch (keyEvent) {
            case KeyEvent.VK_LEFT: {
                gamer.moveSnake(-1, 0);
                break;
            }
            case KeyEvent.VK_RIGHT: {
                gamer.moveSnake(1, 0);
                break;
            }
            case KeyEvent.VK_DOWN: {
                gamer.moveSnake(0, 1);
                break;
            }
            case KeyEvent.VK_UP: {
                gamer.moveSnake(0, -1);
                break;
            }
        }
    }

    public void keyActivity(int keyEvent) {
        SnakesProto.GameMessage.SteerMsg.Builder steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();

        if (gamer == null) return;
        if (gamer.isMaster()) {
            masterKeyPressed(keyEvent);
        }
    }

    public void start() {
        snakeGUI.setVisible(true);
        try {
            messageManager.subscribeReceiver();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void leaveGame() {

    }

    void pingProcessing(InetAddress inetAddress, int port) {
        //if (game == null) return;
        //game.addAliveGamer(inetAddress, port);
    }
}

