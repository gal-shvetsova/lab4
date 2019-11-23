package fit.networks.controller;

import com.google.protobuf.Message;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MultiControllerImpl implements MultiController {

    Thread receiveThread;
    private BlockingQueue<SnakesProto.GameMessage> receivedMessage = new LinkedBlockingQueue<>(Protocol.getMessageQueueCapacity());

    public MultiControllerImpl() {
        receiveThread = new Thread(() -> {
            int port = Protocol.getMulticastPort();
            try (MulticastSocket socket = new MulticastSocket(port)) {
                InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                socket.joinGroup(inetAddress);
                while (true) {
                    byte[] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    socket.receive(packet);
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);
                    receivedMessage.put(protoMessage);
                }
            } catch (Exception e) {

            }
        });
        receiveThread.start();
    }

    public Queue<Message> receiveAllMessages() {
        Queue<Message> messages = new ArrayDeque<>();
        receivedMessage.drainTo(messages);
        return messages;
    }

    @Override
    public SnakesProto.GameMessage receiveMessage(){
        try {
            return receivedMessage.take();
        } catch (InterruptedException ex){  //TODO: check second new game
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendMessage(Message message) {
        int port = Protocol.getMulticastPort();
        try (MulticastSocket socket = new MulticastSocket(port)) {
            InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
            byte[] byteMessage = message.toByteArray();
            DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, inetAddress, port);
            socket.send(packet);

        } catch (Exception e) {

        }

    }
}
