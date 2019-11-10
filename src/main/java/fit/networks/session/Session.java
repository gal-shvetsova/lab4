package fit.networks.session;


import fit.networks.gamer.Gamer;
import fit.networks.protocol.SnakesProto;
import fit.networks.session.tasks.MulticastSender;

import javax.sound.sampled.Port;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Session {

    private final InetAddress multicastAddress;
    private final Integer multicastPort;
    private Gamer gamer;
    private Set<Map.Entry<InetAddress, Port>> availableServers = new HashSet<>();

    private class AvailableServersReceiver extends Thread {
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
                }
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public Session(String name, InetAddress ipAddress, int port) throws Exception{
        this.multicastAddress =  InetAddress.getByName("224.0.0.0");
        this.multicastPort = 5050;
        this.gamer = new Gamer(name, ipAddress, port);
    }

    public void start(){
        Timer timer = new Timer();
        try {
            SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
            SnakesProto.GameMessage.PingMsg.Builder messagePng = SnakesProto.GameMessage.PingMsg.newBuilder();
            message.setPing(messagePng);
            message.setMsgSeq(0);
            AvailableServersReceiver receiver = new AvailableServersReceiver();
            receiver.start();
            MulticastSender sender = new MulticastSender(multicastAddress, multicastPort, message);
            timer.schedule(sender, 0, 100);

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
