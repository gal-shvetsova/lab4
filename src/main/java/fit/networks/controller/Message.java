package fit.networks.controller;

import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;

public class Message implements Comparable{
    private SnakesProto.GameMessage message;
    private InetAddress inetAddress;
    private int port;

    public Message(SnakesProto.GameMessage message, InetAddress inetAddress, int port) {
        this.message = message;
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public SnakesProto.GameMessage getProtoMessage() {
        return message;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Message)) return -1;
        return (int)(message.getMsgSeq() - ((Message) o).message.getMsgSeq());
    }
}
