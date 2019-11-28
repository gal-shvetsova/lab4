package fit.networks.controller;

import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageControllerImpl implements MessageController {

    Thread receiveThread;
    Thread receiveMulticastThread;
    private MulticastSocket socket;
    private static MessageController messageController = null;

        private PriorityBlockingQueue<Message> receivedMessages = new PriorityBlockingQueue<>(
            Protocol.getMessageQueueCapacity(), (o1, o2) -> (int) (o1.getProtoMessage().getMsgSeq() - o2.getProtoMessage().getMsgSeq()));

    private MessageControllerImpl(InetAddress senderAddress, int senderPort) throws IOException {
        this.socket = new MulticastSocket(senderPort);
        this.socket.setInterface(senderAddress);

        receiveMulticastThread = new Thread(() -> {
            int port = Protocol.getMulticastPort();
            try (MulticastSocket socket = new MulticastSocket(port)) {
                socket.setInterface(senderAddress);
                InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
                socket.joinGroup(inetAddress);
                while (true) {
                    byte[] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    socket.receive(packet);
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);
                    receivedMessages.add(new Message(protoMessage, packet.getAddress(), packet.getPort()));
                }
            } catch (Exception e) {
                this.socket.close(); //TODO: make error
            }
        });
        receiveThread = new Thread(() -> {
            try {
                socket.setInterface(senderAddress);
                while (true) {
                    byte[] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    socket.receive(packet);
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);
                    receivedMessages.add(new Message(protoMessage, packet.getAddress(), packet.getPort()));
                }
            } catch (Exception e) {
                this.socket.close();
            }
        });
        receiveThread.start();
        receiveMulticastThread.start();
    }

    public static void startMessageController(InetAddress inetAddress, int port){
        try {
            messageController = new MessageControllerImpl(inetAddress, port);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }


    public static MessageController getMessageController() {
        return messageController;
    }

    @Override
    public Message receiveMessage() {
        try {
            return receivedMessages.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("queue was broken", e);
        }
    }


    @Override
    public void sendMessage(Message message) {
        byte[] byteMessage = message.getProtoMessage().toByteArray();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, message.getInetAddress(), message.getPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Queue<Message> receiveMessages() {
        Queue<Message> messages = new PriorityQueue<>();
        receivedMessages.drainTo(messages);
        return messages;
    }

}
