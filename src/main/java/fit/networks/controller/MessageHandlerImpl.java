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
    private static MessageHandlerImpl messageHandler;
    private final GameController gameController;

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
        this.gameController = GameControllerImpl.getInstance();
    }

    public static MessageHandler getInstance() {
        if (messageHandler == null) {
            messageHandler = new MessageHandlerImpl();
        }
        return messageHandler;
    }

    private void handlePing(Message message){
        gameController.addAliveGamer(message.getInetAddress(), message.getPort());
    }

    private void handleSteer(Message message){
        InetAddress inetAddress = message.getInetAddress();
        int port = message.getPort();
        gameController.changeSnakeDirection(inetAddress, port, ProtoUtils.getDirection(message.getProtoMessage().getSteer().getDirection()));
        sendAck(message);
    }

    private void sendAck(Message message) {
        InetAddress inetAddress = message.getInetAddress();
        int port = message.getPort();
        Message ack = new Message(MessageCreator.makeAckMsg(message, -1, -1), inetAddress, port);
        MessageControllerImpl.getInstance().sendMessage(ack, false);
    }

    private void handleAck(Message message){
        MessageControllerImpl.getInstance().confirmMessage(message);
    }

    private void handleState(Message message){
        int stateId = -1;
        if (gameController.getGame() != null) {
           stateId = gameController.getGame().getGameStateId();
        }
        SnakesProto.GameState state = message.getProtoMessage().getState().getState();
        if (stateId > state.getStateOrder()){
            return;
        }
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
                Gamer gamer = new Gamer(player.getName(), inetAddress, player.getPort(), gameConfig, ProtoUtils.getRole(player.getRole()), player.getId(), player.getScore());
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
            snake.setState(ProtoUtils.getState(s.getState()));
        }
        gameController.setGame(game);
        gameController.loadNewState();
    }

    private void handleAnnouncement(Message message) {
        try {
            SnakesProto.GamePlayer master = ProtoHelper.getMaster(message.getProtoMessage().getAnnouncement().getPlayers());
            if (master == null) {
                return;
            }
            InetAddress masterAddress = InetAddress.getByName(master.getIpAddress());
            int masterPort = master.getPort();
            gameController.addAvailableGame(masterAddress, masterPort, message.getProtoMessage().getAnnouncement());
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
    }

    private void handleJoin(Message message){
        String name = message.getProtoMessage().getJoin().getName();
        gameController.hostGame(name, message.getInetAddress(), message.getPort());
        sendAck(message);
    }

    private void handleError(Message message){
        sendAck(message);
    }

    private void handleRoleChange(Message message){
        switch (message.getProtoMessage().getRoleChange().getReceiverRole()){
            case NORMAL:
            case VIEWER:
                gameController.becomeViewer(message.getInetAddress(), message.getPort());
            case DEPUTY:
                gameController.becomeDeputy();
                break;
            case MASTER:
                gameController.becomeMaster();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message.getProtoMessage().getRoleChange().getReceiverRole());
        }
        sendAck(message);
    }

    private void handleTypeNotSet(Message message){

    }
}
