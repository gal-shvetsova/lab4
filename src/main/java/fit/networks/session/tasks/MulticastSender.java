package fit.networks.session.tasks;

import fit.networks.protocol.SnakesProto;
import fit.networks.session.Session;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.TimerTask;

public class MulticastSender extends TimerTask {
    private InetAddress address;
    private Integer port;
    private SnakesProto.GameMessage.Builder message;

    public MulticastSender(InetAddress address, Integer port, SnakesProto.GameMessage.Builder message) throws Exception{
        if (address.isMulticastAddress())
            this.address = address;
        else
            throw new Exception("Address is not multicast");
        this.port = port;
        this.message = message;
    }

    @Override
    public void run() {
        try(MulticastSocket socket = new MulticastSocket(port)){
            byte [] messageByte = message.build().toByteArray();
            socket.joinGroup(address);
            DatagramPacket packet = new DatagramPacket(messageByte, messageByte.length, address, port);
            socket.send(packet);
            message.setMsgSeq(message.getMsgSeq() + 1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
