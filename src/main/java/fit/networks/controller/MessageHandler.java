package fit.networks.controller;

import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;

public interface MessageHandler {
    void handle(Message message);
}
