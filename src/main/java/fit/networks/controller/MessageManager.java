package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.game.snake.Snake;
import fit.networks.gamer.Gamer;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageManager {
    private AtomicInteger messageSeq = new AtomicInteger(0);
    private SnakeController controller;
    private Timer timer = new Timer();
    private PingSender pingSender = null;
    private AnnouncementSender announcementSender = null;
    private static boolean exist = false;
    private MulticastReceiver multicastReceiver = null;

    private MessageManager(){
       super();
    }

    private MessageManager(SnakeController controller){
        this.controller = controller;
    }

    public static MessageManager createMessageManager(SnakeController controller){
        if (!exist) {
            exist = true;
            return new MessageManager(controller);
        }
        return null;
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
                            controller.pingProcessing(packet.getAddress(), packet.getPort());
                            break;
                        }
                        case ANNOUNCEMENT: {
                            System.out.println(SnakesProto.GameMessage.parseFrom(actualMessage));

                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

  /*  private class MessageReceiver extends Thread {
        @Override
        public void run() {
            try (Socket socket = new Socket(controller.getInetAddress(), controller.getPort())) {
                while (!isInterrupted()) {
                    byte[] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    socket.receive(packet);
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);

                    switch (protoMessage.getTypeCase()) {
                        case PING: {
                            controller.pingProcessing(packet.getAddress(), packet.getPort());
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
*/

    private class AnnouncementSender extends TimerTask {
        private Gamer gamer;

        public AnnouncementSender(Gamer gamer){
            this.gamer = gamer;
        }

        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket(Protocol.getMulticastPort())) {
                SnakesProto.GameMessage gameMessage = makeAnnouncementMessage(gamer.getGame());
                byte[] messageByte = gameMessage.toByteArray();
                InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                socket.joinGroup(inetAddress);
                DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, inetAddress, Protocol.getMulticastPort());
                socket.send(packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class PingSender extends TimerTask {
        @Override
        public void run() {
            try (MulticastSocket socket = new MulticastSocket(Protocol.getMulticastPort())) {
                SnakesProto.GameMessage message = makePingMessage();
                byte[] messageByte = message.toByteArray();
                InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                socket.joinGroup(inetAddress);
                DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, inetAddress, Protocol.getMulticastPort());
                socket.send(packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void subscribeReceiver(){
        if (multicastReceiver != null) return;
        multicastReceiver = new MulticastReceiver();
        multicastReceiver.start();
    }

    public void unsubscribeReceiver(){
        if (multicastReceiver == null) return;
        multicastReceiver.interrupt();
    }

    public void schedulePing() {
        if (pingSender != null) return;
        pingSender = new PingSender();
        timer.schedule(pingSender, 0, 100);
    }


    public void scheduleAnnouncement(Gamer gamer){
        if (announcementSender != null) return;
        announcementSender = new AnnouncementSender(gamer);
        timer.schedule(announcementSender, 0, 1000);
    }

    public void cancelPing(){
        if (pingSender == null) return;
        pingSender.cancel();
        pingSender = null;
    }

    public void cancelAnnouncement(){
        if (announcementSender == null) return;
        announcementSender.cancel();
        announcementSender = null;
    }


    public SnakesProto.GameMessage makePingMessage() {
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GameMessage.PingMsg.Builder pingMessage = SnakesProto.GameMessage.PingMsg.newBuilder();
        message.setMsgSeq(messageSeq.addAndGet(1));
        message.setPing(pingMessage.build());
        return message.build();
    }

    private SnakesProto.GameMessage makeSteerMsg(Direction direction) {
        SnakesProto.GameMessage.SteerMsg.Builder steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        message.setMsgSeq(messageSeq.getAndAdd(1));
        steerMessage.setDirection(direction.makeProtoDirection());
        message.setSteer(steerMessage);
        return message.build();
    }

    private SnakesProto.GameMessage makeJoinMsg(String name) {
        SnakesProto.GameMessage.JoinMsg.Builder joinMessage = SnakesProto.GameMessage.JoinMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        message.setMsgSeq(messageSeq.getAndAdd(1));
        message.setJoin(joinMessage);
        joinMessage.setName(name);
        return message.build();
    }


    private SnakesProto.GamePlayer makeGamePlayer(Gamer gamer){
        SnakesProto.GamePlayer.Builder gamePlayer = SnakesProto.GamePlayer.newBuilder();
        gamePlayer.setName(gamer.getName());
        gamePlayer.setId(gamer.getId());
        gamePlayer.setIpAddress(gamer.getIpAddress().getHostName()); //TODO: check it
        gamePlayer.setPort(gamer.getPort());
        if (gamer.isMaster()) {
            gamePlayer.setRole(SnakesProto.NodeRole.MASTER);
        } else if (gamer.isZombie()) {
            gamePlayer.setRole(SnakesProto.NodeRole.VIEWER);
        } else {
            gamePlayer.setRole(SnakesProto.NodeRole.NORMAL);
        }
        gamePlayer.setScore(gamer.getPoints());
        return gamePlayer.build();
    }

    private SnakesProto.GameConfig makeGameConfig(GameConfig gameConfig){
        SnakesProto.GameConfig.Builder gameConfigProto = SnakesProto.GameConfig.newBuilder();
        gameConfigProto.setWidth(gameConfig.getWidth());
        gameConfigProto.setHeight(gameConfig.getHeight());
        gameConfigProto.setFoodStatic(gameConfig.getFoodStatic());
        gameConfigProto.setFoodPerPlayer((float)gameConfig.getFoodPerPlayer());
        gameConfigProto.setStateDelayMs(gameConfig.getDelayMs());
        gameConfigProto.setDeadFoodProb((float)gameConfig.getDeadFoodProb());
        return gameConfigProto.build();
    }

    private SnakesProto.GameMessage makeAnnouncementMessage(Game game){
        SnakesProto.GameMessage.AnnouncementMsg.Builder announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder();
        SnakesProto.GameMessage.Builder msg = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GamePlayers.Builder gamePlayers = SnakesProto.GamePlayers.newBuilder();

        for (Gamer gamer : game.getActiveGamers()){
            gamePlayers.addPlayers(makeGamePlayer(gamer));
        }

        announcementMsg.setPlayers(gamePlayers);
        announcementMsg.setConfig(makeGameConfig(game.getGameConfig()));

        msg.setMsgSeq(messageSeq.getAndAdd(1));
        msg.setAnnouncement(announcementMsg);
        return msg.build();
    }

}
