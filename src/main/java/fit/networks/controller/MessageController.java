package fit.networks.controller;

import java.net.InetAddress;
import java.util.Queue;

public interface MessageController {
     void sendMessage(Message message, boolean needConfirm);
     Queue<Message> receiveMessages();
     void confirmMessage(Message message);
     void resendMessages(InetAddress inetAddress, int port);
     Message receiveMessage();
}
