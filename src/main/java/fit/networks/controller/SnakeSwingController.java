package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.gui.SnakeGUI;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;
import java.awt.event.KeyEvent;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class SnakeSwingController extends SnakeController {
    private SnakeGUI snakeGUI;
    private Game game;
    private String name;
    private InetAddress inetAddress;
    private int port;
    private Set<Game> availableServers = new HashSet<>();  //запущенные игры


    public SnakeSwingController(String name, InetAddress inetAddress, int port) throws Exception {
        this.snakeGUI = new SnakeGUI(this);
        this.name = name;
        this.inetAddress = inetAddress;
        this.port = port;
        this.game = null;
    }

    private class MulticastReceiver extends Thread {
        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket(Protocol.getMulticastPort())) {
                socket.joinGroup(InetAddress.getByName(Protocol.getMulticastAddressName()));
                while (!isInterrupted()) {
                    byte[] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    socket.receive(packet);
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);

                    switch (protoMessage.getTypeCase()) {
                        case PING: {
                            game.addAliveGamer(packet.getAddress(), packet.getPort());
                            break;
                        }
                        case ANNOUNCEMENT: {

                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class GUIUpdater extends TimerTask {
        @Override
        public void run() {

            if (game != null && game.getGamer() != null && game.getGamer().getSnake() != null) {
                if (!game.getGamer().getSnake().isAlive()) {
                    snakeGUI.showDeadForm();
                }
                else
                    game.getGamer().getSnake().run();
                snakeGUI.loadNewField(game.makeRepresentation());
            }
        }
    }

    public void startNewGame(String width, String height, String foodStatic, String foodPerPlayer, String delayMs,
                             String deadFoodProb) throws Exception {
        game = new Game();
        game.startNewGame(name, inetAddress, port, Integer.parseInt(width), Integer.parseInt(height),
                Integer.parseInt(foodStatic), Float.parseFloat(foodPerPlayer), Integer.parseInt(delayMs),
                Float.parseFloat(deadFoodProb));
        snakeGUI.startGame(Integer.parseInt(width), Integer.parseInt(height), Integer.parseInt(foodStatic),
                Float.parseFloat(foodPerPlayer), Integer.parseInt(delayMs), Float.parseFloat(deadFoodProb));
        game.start();

    }

    public void keyActivity(int keyEvent) {
        if (game.getGamer().isMaster()) {
            switch (keyEvent) {
                case KeyEvent.VK_LEFT: {
                    game.moveSnake(-1, 0);
                    break;
                }
                case KeyEvent.VK_RIGHT: {
                    game.moveSnake(1, 0);
                    break;
                }
                case KeyEvent.VK_DOWN: {
                    game.moveSnake(0, 1);
                    break;
                }
                case KeyEvent.VK_UP: {
                    game.moveSnake(0, -1);
                    break;
                }
            }
        } //TODO: else send to master
    }

    public void start() {
        snakeGUI.setVisible(true);
        try {
            MulticastReceiver receiver = new MulticastReceiver();
            receiver.start();
            Timer timer = new Timer();
            timer.schedule(new GUIUpdater(), 0, 100);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void leaveGame() {

    }
}

