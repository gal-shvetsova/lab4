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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

public class MessageControllerImpl implements MessageController {

    private static boolean initialized = false;
    Thread receiveThread;
    Thread receiveMulticastThread;
    private MulticastSocket socket;
    private static MessageController messageController = null;

    private PriorityBlockingQueue<Message> receivedMessages = new PriorityBlockingQueue<>(
            Protocol.getMessageQueueCapacity(), (o1, o2) ->
                (int) (o1.getProtoMessage().getMsgSeq() - o2.getProtoMessage().getMsgSeq()));

    private PriorityBlockingQueue<Message> messagesToConfirm = new PriorityBlockingQueue<>(
            Protocol.getMessageQueueCapacity(), (o1, o2) ->
            (int) (o1.getProtoMessage().getMsgSeq() - o2.getProtoMessage().getMsgSeq()));

    private MessageControllerImpl(InetAddress senderAddress, int senderPort) throws IOException {
        this.socket = new MulticastSocket(senderPort);
        this.socket.setInterface(senderAddress);
        Timer timer = new Timer();
        TimerTask resendTask = new TimerTask() {
            @Override
            public void run() {
                for (Message message : messagesToConfirm) {
                    sendMessage(message, false);
                }
            }
        };

        timer.schedule(resendTask,1000, 1000);

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
                this.socket.close();
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

    public static void init(InetAddress inetAddress, int port) {
        initialized = true;
        try {
            messageController = new MessageControllerImpl(inetAddress, port);
        } catch (Exception ex) {
            throw new RuntimeException("Could not initialized MessageControllerImpl", ex);
        }
    }

    public static MessageController getInstance() {
        if (!initialized) {
            throw new RuntimeException("MessageController's not initialized");
        }
        return messageController;
    }

    @Override
    public void sendMessage(Message message, boolean needConfirm) {
        byte[] byteMessage = message.getProtoMessage().toByteArray();
        DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, message.getInetAddress(), message.getPort());
        if (needConfirm){
            messagesToConfirm.add(message);
        }
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

    @Override
    public void confirmMessage(Message message) {
        messagesToConfirm.removeIf(message1 -> message1.getProtoMessage().getMsgSeq() == message.getProtoMessage().getMsgSeq()); //todo check
    }

    @Override
    synchronized public void resendMessages(InetAddress inetAddress, int port) {
        for (Message message : receivedMessages) {
            Message msg = new Message(message.getProtoMessage(), inetAddress, port);
        }
    }

}
