package fit.networks.controller;

import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class MessageControllerImpl implements MessageController {

    Thread receiveThread;
    Thread receiveMulticastThread;
    private MulticastSocket socket;
    private AtomicReference<CompanionNetInfo> companionNetInfo = new AtomicReference<>();

    private PriorityBlockingQueue<SnakesProto.GameMessage> receivedMessages = new PriorityBlockingQueue<>(
            Protocol.getMessageQueueCapacity(), (o1, o2) -> (int) (o1.getMsgSeq() - o2.getMsgSeq()));

    public MessageControllerImpl(InetAddress senderAddress, int senderPort, InetAddress receiverAddress, int receiverPort) throws IOException {
        setAddressAndPort(receiverAddress, receiverPort);
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
                    if (packet.getAddress() == this.companionNetInfo.get().getCompanionAddress() && packet.getPort() == this.companionNetInfo.get().getCompanionPort()) {
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
        try {
            return receivedMessages.take();
        } catch (InterruptedException e) {
            throw new RuntimeException("queue was broken", e);
        }
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
    public void sendMessage(SnakesProto.GameMessage message) {
        byte[] byteMessage = message.toByteArray();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, companionNetInfo.get().getCompanionAddress(), companionNetInfo.get().getCompanionPort());
        try {
            socket.send(packet);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void changeCompanion(InetAddress companionAddress, int companionPort) {
        setAddressAndPort(companionAddress, companionPort);
    }

    synchronized private void setAddressAndPort(InetAddress companionAddress, int companionPort) {
        this.companionNetInfo.set(new CompanionNetInfo(companionAddress, companionPort));
    }

    public static class CompanionNetInfo {
        private InetAddress companionAddress;
        private int companionPort;

        public CompanionNetInfo(InetAddress companionAddress, int companionPort) {
            this.companionAddress = companionAddress;
            this.companionPort = companionPort;
        }

        public InetAddress getCompanionAddress() {
            return companionAddress;
        }

        public int getCompanionPort() {
            return companionPort;
        }
    }
}
