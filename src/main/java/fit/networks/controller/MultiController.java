package fit.networks.controller;

import com.google.protobuf.Message;
import fit.networks.protocol.SnakesProto;

public interface MultiController {
     SnakesProto.GameMessage receiveMessage();
     void sendMessage(Message message);
}
