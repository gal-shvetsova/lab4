package fit.networks.controller;

import fit.networks.game.Coordinates;
import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Snake;
import fit.networks.gamer.Gamer;
import fit.networks.protocol.ProtoHelper;
import fit.networks.protocol.SnakesProto;
import fit.networks.util.ProtoUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MessageHandlerImpl implements MessageHandler {
    private static MessageHandler messageHandler = null;
    private Logger logger = Logger.getLogger("message handler");

    @Override
    public void handle(Message message) {
     //   logger.info(message.getProtoMessage().getTypeCase().toString());
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

    public static MessageHandler getInstance() {
        if (messageHandler == null) {
            messageHandler = new MessageHandlerImpl();
        }
        return messageHandler;
    }

    private void handlePing(Message message){
        GameControllerImpl.getController().addAliveGamer(message.getInetAddress(), message.getPort());
    }

    private void handleSteer(Message message){
        logger.info("handle steer");
        GameControllerImpl.getController().changeSnakeDirection(message.getInetAddress(), message.getPort(), ProtoUtils.getDirection(message.getProtoMessage().getSteer().getDirection()));
    }

    private void handleAck(Message message){
        MessageControllerImpl.getInstance().confirmMessage(message);
    }

    private void handleState(Message message){
        SnakesProto.GameState state = message.getProtoMessage().getState().getState();
        double deadFoodProb = state.getConfig().getDeadFoodProb();
        int delayMs = state.getConfig().getStateDelayMs();
        double foodPerPlayer = state.getConfig().getFoodPerPlayer();
        int foodStatic  = state.getConfig().getFoodStatic();
        int height = state.getConfig().getHeight();
        int width = state.getConfig().getWidth();
        GameConfig gameConfig = new GameConfig(width, height, foodStatic, foodPerPlayer, delayMs, deadFoodProb);

        Deque<Coordinates> foods = state.getFoodsList()
                .stream()
                .map(x -> Coordinates.of(x.getX(), x.getY()))
                .collect(Collectors.toCollection(ArrayDeque::new));

        Game game = new Game(gameConfig, foods);

        try {
            for (SnakesProto.GamePlayer player : state.getPlayers().getPlayersList()) {
                InetAddress inetAddress = InetAddress.getByName(player.getIpAddress());
                Gamer gamer = new Gamer(player.getName(), inetAddress, player.getPort(), gameConfig, ProtoUtils.getRole(player.getRole()), player.getId());
                game.addGamer(gamer);

            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        for (SnakesProto.GameState.Snake s: state.getSnakesList()) {
            Snake snake = new Snake(Coordinates.of(width, height));
            Deque<Coordinates> keyPoints = new ArrayDeque<>();
            s.getPointsList().forEach(x -> keyPoints.addLast(ProtoUtils.getCoordinates(x)));
            snake.setKeyPoints(keyPoints);
            snake.setDirection(ProtoUtils.getDirection(s.getHeadDirection()));
            game.getGamerById(s.getPlayerId()).setSnake(snake);
        }

        GameControllerImpl.getController().setGame(game);
        GameControllerImpl.getController().loadNewState();
        //todo: use state id

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
        logger.info("handle join");
        String name = message.getProtoMessage().getJoin().getName();
        GameControllerImpl.getController().hostGame(name, message.getInetAddress(), message.getPort());
        Message ackMessage = new Message(MessageCreator.makeAckMsg(message), message.getInetAddress(), message.getPort());
        MessageControllerImpl.getInstance().sendMessage(ackMessage);

    }

    private void handleError(Message message){

    }

    private void handleRoleChange(Message message){
        switch (message.getProtoMessage().getRoleChange().getReceiverRole()){
            case NORMAL:
            case VIEWER:
            case DEPUTY:
                break;
            case MASTER:
                GameControllerImpl.getController().becomeMaster();
        }

    }

    private void handleTypeNotSet(Message message){

    }
}
