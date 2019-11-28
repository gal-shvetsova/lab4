package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.protocol.ProtoHelper;
import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class MessageHandlerImpl implements MessageHandler {
    private static MessageHandler messageHandler = null;

    @Override
    public void handle(Message message) {
        switch (message.getProtoMessage().getTypeCase()) {
            case PING:
                handlePing(message);
                break;
            case STEER:
                handleSteer(message);
                break;
            case ACK:
                handleAck(message);
                break;
            case STATE:
                handleState(message);
                break;
            case ANNOUNCEMENT:
                handleAnnouncement(message);
                break;
            case JOIN:
                handleJoin(message);
                break;
            case ERROR:
                handleError(message);
                break;
            case ROLE_CHANGE:
                handleRoleChange(message);
                break;
            case TYPE_NOT_SET:
                handleTypeNotSet(message);
                break;
        }
    }

    private MessageHandlerImpl() {
        super();
    }

    public static MessageHandler getMessageHandler() {
        if (messageHandler == null) {
            messageHandler = new MessageHandlerImpl();
        }
        return messageHandler;
    }

    private void handlePing(Message message){
        GameControllerImpl.getController().addAliveGamer(message.getInetAddress(), message.getPort());
    }

    private void handleSteer(Message message){

    }

    private void handleAck(Message message){

    }

    private void handleState(Message message){
        SnakesProto.GameState protoMsg = message.getProtoMessage().getState().getState();
        double deadFoodProb = protoMsg.getConfig().getDeadFoodProb();
        int delayMs = protoMsg.getConfig().getStateDelayMs();
        double foodPerPlayer = protoMsg.getConfig().getFoodPerPlayer();
        int foodStatic  = protoMsg.getConfig().getFoodStatic();
        int height = protoMsg.getConfig().getHeight();
        int width = protoMsg.getConfig().getWidth();
        GameConfig gameConfig = new GameConfig(width, height, foodStatic, foodPerPlayer, delayMs, deadFoodProb);
        Game game = new Game(gameConfig);

    }

    private void handleAnnouncement(Message message) {
        try {
            SnakesProto.GamePlayer master = ProtoHelper.getMaster(message.getProtoMessage().getAnnouncement().getPlayers());
            if (master == null) {
                return;
            }
            InetAddress masterAddress = InetAddress.getByName(master.getIpAddress());
            int masterPort = master.getPort();
            GameControllerImpl.getController().addAvailableGame(masterAddress, masterPort, message.getProtoMessage().getAnnouncement());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    private void handleJoin(Message message){

    }

    private void handleError(Message message){

    }

    private void handleRoleChange(Message message){

    }

    private void handleTypeNotSet(Message message){

    }
}
