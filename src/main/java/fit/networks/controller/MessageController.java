package fit.networks.controller;

import fit.networks.protocol.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Queue;

public interface MessageController {
     Message receiveMessage();
     void sendMessage(Message message);
     Queue<Message> receiveMessages();
}
