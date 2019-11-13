package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.Gamer;
import fit.networks.gui.SnakeGUI;
import fit.networks.protocol.SnakesProto;


import javax.sound.sampled.Port;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SnakeSwingController extends SnakeController {
    private SnakeGUI snakeGUI;
    private Game game;
    private Gamer gamer;
    private final InetAddress multicastAddress;
    private final Integer multicastPort;
    private Set<Map.Entry<InetAddress, Port>> availableServers = new HashSet<>();  //запущенные игры
    private AtomicInteger messageSeq = new AtomicInteger(0);

    public SnakeSwingController(String name, InetAddress inetAddress, int port) throws Exception {
        this.snakeGUI = new SnakeGUI(this);
        this.gamer = new Gamer(name, inetAddress, port);
        this.game = null;
        this.multicastAddress =  InetAddress.getByName("224.0.0.0");
        this.multicastPort = 5050;
    }

    private class Receiver extends Thread {
        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket(multicastPort)){
                socket.joinGroup(multicastAddress);
                while (!isInterrupted()){
                    byte [] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    socket.receive(packet);
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);

                    switch (protoMessage.getTypeCase()){
                        case PING:{
                           // game.treatPingMessage(packet.getAddress(), packet.getPort());
                            break;
                        }
                        case STEER:{

                        }
                        case ACK:{

                        }
                        case JOIN:{

                        }
                        case ERROR:{

                        }
                        case STATE:{

                        }
                        case ROLE_CHANGE:{

                        }
                        case ANNOUNCEMENT:{

                        }

                    }
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public class PingSender extends TimerTask {
        @Override
        public void run() {
            try(MulticastSocket socket = new MulticastSocket(multicastPort)){
                SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
                SnakesProto.GameMessage.PingMsg.Builder pingMessage = SnakesProto.GameMessage.PingMsg.newBuilder();
                message.setMsgSeq(messageSeq.getAndAdd(1));
                message.setPing(pingMessage.build());
                byte [] messageByte = message.build().toByteArray();
                socket.joinGroup(multicastAddress);
                DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, multicastAddress, multicastPort);
                socket.send(packet);
                socket.send(packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void startNewGame(int width, int height, int foodStatic, float foodPerPlayer, int delayMs,
                             float deadFoodProb) throws Exception{
        game = new Game(gamer,  width,  height,  foodStatic,  foodPerPlayer,  delayMs,  deadFoodProb);
    }

    public void startNewGame(String width, String height, String foodStatic, String foodPerPlayer, String delayMs,
                             String deadFoodProb) throws Exception{
        game = new Game(gamer,  Integer.parseInt(width),  Integer.parseInt(height),  Integer.parseInt(foodStatic),
                Float.parseFloat(foodPerPlayer),  Integer.parseInt(delayMs),  Float.parseFloat(deadFoodProb));
        System.out.println("game" + game);
        snakeGUI.startGame(Integer.parseInt(width),  Integer.parseInt(height),  Integer.parseInt(foodStatic),
                Float.parseFloat(foodPerPlayer),  Integer.parseInt(delayMs),  Float.parseFloat(deadFoodProb));
    }

    public void start(){
        snakeGUI.setVisible(true);
        Timer timer = new Timer();
        try {
            Receiver receiver = new Receiver();
            receiver.start();
            PingSender sender = new PingSender();
            timer.schedule(sender, 0, 100);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}

