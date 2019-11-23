package fit.networks.controller;

import com.google.protobuf.Message;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.net.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SingleControllerImpl implements SingleController {
    private InetAddress interlocutorAddress;
    private int interlocutorPort;
    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;
    private Thread receiveThread;
    private BlockingQueue<Message> receivedMessages = new LinkedBlockingQueue<>(Protocol.getMessageQueueCapacity());

    public SingleControllerImpl(InetAddress localAddress, int senderPort, InetAddress interlocutorAddress, int interlocutorPort) throws SocketException {

        this.receiveSocket = new DatagramSocket(interlocutorPort);
        this.sendSocket = new DatagramSocket(senderPort, localAddress);
        this.interlocutorAddress = interlocutorAddress;

        receiveThread = new Thread(() -> {
            try {
                while (true) {
                    byte[] message = new byte[10000];
                    DatagramPacket packet = new DatagramPacket(message, 10000);
                    receiveSocket.receive(packet);
                    if (packet.getAddress() != interlocutorAddress) continue;
                    byte[] actualMessage = new byte[packet.getLength()];
                    System.arraycopy(packet.getData(), 0, actualMessage, 0, packet.getLength());
                    SnakesProto.GameMessage protoMessage = SnakesProto.GameMessage.parseFrom(actualMessage);
                    receivedMessages.put(protoMessage);
                }
            } catch (Exception e) {

            }
        });


    }

    @Override
    public void sendMessage(Message message) {
        try {
            byte [] byteMessage = message.toByteArray();
            DatagramPacket justMessage = new DatagramPacket(byteMessage, byteMessage.length, interlocutorAddress, interlocutorPort);
            sendSocket.send(justMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Message receiveMessage() {
        return null;
    }

    public void changeReceiver(int port, InetAddress inetAddress){
        try {
            receiveThread.wait();
            receiveSocket = new DatagramSocket(port, inetAddress);
            interlocutorPort = port;
            interlocutorAddress = inetAddress;
            receiveThread.notify();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
