package fit.networks.game;

import fit.networks.game.snake.Coordinates;
import fit.networks.protocol.ProtoMaker;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;
import fit.networks.game.snake.Snake;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private GameConfig gameConfig = new GameConfig();
    private Gamer gamer; //todo make gamer part of controller
    private ArrayList<Gamer> activeGamers = new ArrayList<>();
    private ArrayList<Gamer> activeGamersPerCycle = new ArrayList<>();
    private ProtoMaker protoMaker = new ProtoMaker();



    public class PingSender extends TimerTask {
        @Override
        public void run() {
            try(MulticastSocket socket = new MulticastSocket(Protocol.getMulticastPort())){
                SnakesProto.GameMessage message = protoMaker.makePingMessage();
                byte [] messageByte = message.toByteArray();
                InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                socket.joinGroup(inetAddress);
                DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, inetAddress, Protocol.getMulticastPort());
                socket.send(packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


   /* public class AnnouncementSender extends TimerTask {
        private SnakesProto.GameMessage.AnnouncementMsg makeAnnouncementMessage(){
            SnakesProto.GameMessage.AnnouncementMsg.Builder announcementMessage = SnakesProto.GameMessage.AnnouncementMsg.newBuilder();
            SnakesProto.GamePlayers
        }
        @Override
        public void run() {
            try(MulticastSocket socket = new MulticastSocket(Protocol.getMulticastPort())){
                SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();

                message.setMsgSeq(messageSeq.getAndAdd(1));
                message.setPing(pingMessage.build());
                byte [] messageByte = message.build().toByteArray();
                InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                socket.joinGroup(inetAddress);
                DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, inetAddress, Protocol.getMulticastPort());
                socket.send(packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
*/

   //todo: function of getting unique id

    public void startNewGame(String name, InetAddress inetAddress, int port, int width, int height, int foodStatic,
                             float foodPerPlayer, int delayMs, float deadFoodProb) throws Exception{
        gamer = new Gamer(name, inetAddress, port, 0, true);
        Snake snake = new Snake(gamer, width, height);

        snake.randomStart();
        gamer.setSnake(snake);
        activeGamers.add(gamer);
        gameConfig.setWidth(width);
        gameConfig.setHeight(height);
        gameConfig.setFoodStatic(foodStatic);
        gameConfig.setFoodPerPlayer(foodPerPlayer);
        gameConfig.setDelayMs(delayMs);
        gameConfig.setDeadFoodProb(deadFoodProb);
    }

    public Gamer getGamer(){
        return gamer;
    }

    public void moveSnake(int x, int y){
        gamer.getSnake().changeDirection(x,y);
    }

    public void start(){
        Timer timer = new Timer();

        timer.schedule(new PingSender(), 0, 100);

    }

    public Integer[] makeXRepresentation(){
        Integer[] x = new Integer[gameConfig.getWidth()];
        for (int i = 0; i < gameConfig.getWidth(); i++)
            x[i] = 0;
        for (Gamer gamer: activeGamers) {
            for (Coordinates c : gamer.getSnake()) {
                x[c.getX()] = 1;
            }
        }
        return x;
    }

    public Integer[] makeYRepresentation(){
        Integer[] y = new Integer[gameConfig.getHeight()];
        for (int i = 0; i < gameConfig.getHeight(); i++)
            y[i] = 0;
        for (Gamer gamer: activeGamers) {
            for (Coordinates c : gamer.getSnake()) {
                y[c.getY()] = 1;
            }
        }
        return y;
    }

    public void addAliveGamer(InetAddress inetAddress, int port) {
        activeGamersPerCycle.add(new Gamer(inetAddress, port));
    }
}
