package fit.networks.controller;

import com.google.protobuf.Message;

import java.net.InetAddress;

public interface SingleController {
    void sendMessage(Message message);
    Message receiveMessage();
}
