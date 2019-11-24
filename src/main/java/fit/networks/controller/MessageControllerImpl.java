package fit.networks.controller;

import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageControllerImpl implements MessageController {

    Thread receiveThread;
    Thread receiveMulticastThread;
    private InetAddress companionAddress;
    private int companionPort;
    private MulticastSocket socket;

    private PriorityBlockingQueue<SnakesProto.GameMessage> receivedMessages = new PriorityBlockingQueue<>(
            Protocol.getMessageQueueCapacity(), (o1, o2) -> (int) (o1.getMsgSeq() - o2.getMsgSeq()));

    public MessageControllerImpl(InetAddress senderAddress, int senderPort, InetAddress receiverAddress, int receiverPort) throws IOException {
        this.companionAddress = receiverAddress;
        this.companionPort = receiverPort;
        this.socket = new MulticastSocket(senderPort);
        this.socket.setInterface(senderAddress);

        receiveMulticastThread = new Thread(() -> {
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
                    receivedMessages.add(protoMessage);
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
                    if (packet.getAddress() == receiverAddress && packet.getPort() == receiverPort) {
                        byte[] actualMessage = new byte[packet.getLength()];
                        System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                        SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);
                        receivedMessages.add(protoMessage);
                    }
                }
            } catch (Exception e) {
                this.socket.close();
            }
        });
        receiveThread.start();
        receiveMulticastThread.start();
    }

    @Override
    public SnakesProto.GameMessage receiveMessage() {
        return receivedMessages.poll();
    }

    @Override
    public void sendMulticastMessage(SnakesProto.GameMessage message) throws IOException {
        byte[] byteMessage = message.toByteArray();
        InetAddress inetAddress = InetAddress.getByName(Protocol.getMulticastAddressName());
        int port = Protocol.getMulticastPort();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, inetAddress, port);
        socket.send(packet);
    }

    @Override
    public void sendMessage(SnakesProto.GameMessage message) throws IOException {
        byte[] byteMessage = message.toByteArray();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, companionAddress, companionPort);
        socket.send(packet);
    }

    @Override
    public void changeCompanion(InetAddress companionAddress, int companionPort) {
        this.companionAddress = companionAddress;
        this.companionPort = companionPort;
    }
}
