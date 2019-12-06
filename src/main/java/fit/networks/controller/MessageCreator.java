package fit.networks.controller;

import fit.networks.game.Coordinates;
import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.gamer.Gamer;
import fit.networks.gamer.Role;
import fit.networks.protocol.SnakesProto;
import fit.networks.util.ProtoUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MessageCreator {
    private static AtomicInteger messageSeq = new AtomicInteger(0);
    private static AtomicInteger stateOrder = new AtomicInteger(0);

    private static SnakesProto.GameConfig makeGameConfig(GameConfig gameConfig) {
        SnakesProto.GameConfig.Builder gameConfigProto = SnakesProto.GameConfig.newBuilder();
        gameConfigProto.setWidth(gameConfig.getWidth());
        gameConfigProto.setHeight(gameConfig.getHeight());
        gameConfigProto.setFoodStatic(gameConfig.getFoodStatic());
        gameConfigProto.setFoodPerPlayer((float) gameConfig.getFoodPerPlayer());
        gameConfigProto.setStateDelayMs(gameConfig.getDelayMs());
        gameConfigProto.setDeadFoodProb((float) gameConfig.getDeadFoodProb());
        return gameConfigProto.build();
    }

    private static SnakesProto.GameState.Snake makeSnake(Gamer gamer) {
        return SnakesProto.GameState.Snake.newBuilder()
                .setHeadDirection(ProtoUtils.getProtoDirection(gamer.getSnake().getDirection()))
                .setPlayerId(gamer.getId())
                .addAllPoints(gamer.getSnake().getKeyPoints().stream()
                        .map(point -> SnakesProto.GameState.Coord.newBuilder()
                                .setY(point.getY())
                                .setX(point.getX())
                                .build())
                        .collect(Collectors.toList()))
                .setState(gamer.isDead()
                        ? SnakesProto.GameState.Snake.SnakeState.ALIVE
                        : SnakesProto.GameState.Snake.SnakeState.ZOMBIE)
                .build();
    }

    public static SnakesProto.GameMessage makePingMessage() {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageSeq.addAndGet(1))
                .setPing(SnakesProto.GameMessage.PingMsg.newBuilder().build())
                .build();
    }

    public static SnakesProto.GameMessage makeSteerMsg(Direction direction) {
        SnakesProto.GameMessage.SteerMsg.Builder steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        message.setMsgSeq(messageSeq.getAndAdd(1));
        steerMessage.setDirection(ProtoUtils.getProtoDirection(direction));
        message.setSteer(steerMessage);
        return message.build();
    }

    public static SnakesProto.GameMessage makeJoinMsg(String name) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(messageSeq.getAndAdd(1))
                .setJoin(
                        SnakesProto.GameMessage.JoinMsg.newBuilder().setName(name))
                .build();
    }

    public static SnakesProto.GameMessage makeAckMsg(Message message) {
        return SnakesProto.GameMessage.newBuilder()
                .setMsgSeq(message.getProtoMessage().getMsgSeq())
                .setAck(SnakesProto.GameMessage.AckMsg.newBuilder())
                .build();
    }


    public static SnakesProto.GamePlayer makeGamePlayer(Gamer gamer) {
        return SnakesProto.GamePlayer.newBuilder()
                .setName(gamer.getName())
                .setId(gamer.getId())
                .setIpAddress(gamer.getIpAddress().getHostAddress())
                .setPort(gamer.getPort())
                .setRole(ProtoUtils.getProtoRole(gamer.getRole()))
                .setScore(gamer.getPoints())
                .build();
    }

    public static SnakesProto.GameMessage makeAnnouncementMessage(Game game) {
        SnakesProto.GameMessage.AnnouncementMsg.Builder announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder();
        SnakesProto.GameMessage.Builder msg = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GamePlayers.Builder gamePlayers = makeGamePlayers(game);

        announcementMsg.setPlayers(gamePlayers);
        announcementMsg.setConfig(makeGameConfig(game.getGameConfig()));

        msg.setMsgSeq(messageSeq.getAndAdd(1));
        msg.setAnnouncement(announcementMsg);
        return msg.build();
    }

    private static SnakesProto.GamePlayers.Builder makeGamePlayers(Game game) {
        SnakesProto.GamePlayers.Builder gamePlayers = SnakesProto.GamePlayers.newBuilder();

        for (Gamer gamer : game.getAliveGamers()) {
            gamePlayers.addPlayers(makeGamePlayer(gamer));
        }
        return gamePlayers;
    }

    public static SnakesProto.GameMessage makeStateMessage(Game game) {
        SnakesProto.GameMessage.Builder msg = SnakesProto.GameMessage.newBuilder();
        msg.setMsgSeq(messageSeq.getAndAdd(1));
        SnakesProto.GameMessage.StateMsg.Builder stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder();
        SnakesProto.GameState.Builder gameStateMsg = SnakesProto.GameState.newBuilder();
        gameStateMsg.setStateOrder(stateOrder.getAndAdd(1));

        for (Gamer g : game.getAliveGamers()) {
            gameStateMsg.addSnakes(makeSnake(g));
        }

        for (Coordinates c : game.getFoodCoordinates()) {
            SnakesProto.GameState.Coord.Builder coord = SnakesProto.GameState.Coord.newBuilder();
            coord.setX(c.getX());
            coord.setY(c.getY());
            gameStateMsg.addFoods(coord);
        }

        gameStateMsg.setPlayers(makeGamePlayers(game));
        gameStateMsg.setConfig(makeGameConfig(game.getGameConfig()));
        stateMsg.setState(gameStateMsg);
        msg.setState(stateMsg);
        return msg.build();
    }

    public static SnakesProto.GameMessage makeRoleChangeMessage(Role role) {  //зачем это надо если состояние отправляется периодически
        return SnakesProto.GameMessage
                .newBuilder()
                .setMsgSeq(messageSeq.getAndAdd(1))
                .setRoleChange(SnakesProto.GameMessage.RoleChangeMsg
                        .newBuilder()
                        .setReceiverRole(ProtoUtils.getProtoRole(role))
                        .build())
                .build();
    }


}
