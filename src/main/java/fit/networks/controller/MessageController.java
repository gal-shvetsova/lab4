package fit.networks.controller;

import com.google.protobuf.Message;
import fit.networks.protocol.SnakesProto;

import java.io.IOException;
import java.net.InetAddress;

public interface MessageController {
     SnakesProto.GameMessage receiveMessage();
     void sendMessage(SnakesProto.GameMessage message) throws IOException;
     void sendMulticastMessage(SnakesProto.GameMessage message) throws IOException;
     void changeCompanion(InetAddress companionAddress, int companionPort);
}
