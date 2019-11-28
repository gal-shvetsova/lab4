package fit.networks.controller;

import fit.networks.game.Coordinates;
import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.gamer.Gamer;
import fit.networks.protocol.SnakesProto;
import fit.networks.util.ProtoUtils;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MessageBuilder {
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
                .setHeadDirection(ProtoUtils.get(gamer.getSnake().getDirection()))
                .setPlayerId(gamer.getId())
                .addAllPoints(gamer.getSnake().getKeyPoints().stream()
                        .map(point -> SnakesProto.GameState.Coord.newBuilder()
                                .setY(point.getY())
                                .setX(point.getX())
                                .build())
                        .collect(Collectors.toList()))
                .setState(gamer.isZombie()
                        ? SnakesProto.GameState.Snake.SnakeState.ALIVE
                        : SnakesProto.GameState.Snake.SnakeState.ZOMBIE)
                .build();
    }

    public static SnakesProto.GameMessage makePingMessage() {
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GameMessage.PingMsg.Builder pingMessage = SnakesProto.GameMessage.PingMsg.newBuilder();
        message.setMsgSeq(messageSeq.addAndGet(1));
        message.setPing(pingMessage.build());
        return message.build();
    }

    public static SnakesProto.GameMessage makeSteerMsg(Direction direction) {
        SnakesProto.GameMessage.SteerMsg.Builder steerMessage = SnakesProto.GameMessage.SteerMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        message.setMsgSeq(messageSeq.getAndAdd(1));
        steerMessage.setDirection(ProtoUtils.get(direction));
        message.setSteer(steerMessage);
        return message.build();
    }

    public static SnakesProto.GameMessage makeJoinMsg(String name) {
        SnakesProto.GameMessage.JoinMsg.Builder joinMessage = SnakesProto.GameMessage.JoinMsg.newBuilder();
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        message.setMsgSeq(messageSeq.getAndAdd(1));
        message.setJoin(joinMessage);
        joinMessage.setName(name);
        return message.build();
    }


    public static SnakesProto.GamePlayer makeGamePlayer(Gamer gamer) {
        SnakesProto.GamePlayer.Builder gamePlayer = SnakesProto.GamePlayer.newBuilder();
        gamePlayer.setName(gamer.getName());
        gamePlayer.setId(gamer.getId());
        gamePlayer.setIpAddress(gamer.getIpAddress().getHostAddress()); //TODO: check it
        gamePlayer.setPort(gamer.getPort());
        if (gamer.isMaster()) {
            gamePlayer.setRole(SnakesProto.NodeRole.MASTER);
        } else if (gamer.isZombie()) {
            gamePlayer.setRole(SnakesProto.NodeRole.VIEWER);
        } else {
            gamePlayer.setRole(SnakesProto.NodeRole.NORMAL);
        }
        gamePlayer.setScore(gamer.getPoints());
        return gamePlayer.build();
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

        for (Gamer gamer : game.getActiveGamers()) {
            gamePlayers.addPlayers(makeGamePlayer(gamer));
        }
        return gamePlayers;
    }

    public static SnakesProto.GameMessage makeStateMessage(Gamer gamer) {
        SnakesProto.GameMessage.Builder msg = SnakesProto.GameMessage.newBuilder();
        msg.setMsgSeq(messageSeq.getAndAdd(1));
        SnakesProto.GameMessage.StateMsg.Builder stateMsg = SnakesProto.GameMessage.StateMsg.newBuilder();
        SnakesProto.GameState.Builder gameStateMsg = SnakesProto.GameState.newBuilder();
        gameStateMsg.setStateOrder(stateOrder.getAndAdd(1));

        for (Gamer g:gamer.getGame().getActiveGamers()) {
            gameStateMsg.addSnakes(makeSnake(g));
        }

        for (Coordinates c: gamer.getGame().getFoodCoordinates()){
            SnakesProto.GameState.Coord.Builder coord = SnakesProto.GameState.Coord.newBuilder();
            coord.setX(c.getX());
            coord.setY(c.getY());
            gameStateMsg.addFoods(coord);
        }

        gameStateMsg.setPlayers(makeGamePlayers(gamer.getGame()));
        gameStateMsg.setConfig(makeGameConfig(gamer.getGame().getGameConfig()));
        stateMsg.setState(gameStateMsg);
        msg.setState(stateMsg);
        return msg.build();
    }
}
