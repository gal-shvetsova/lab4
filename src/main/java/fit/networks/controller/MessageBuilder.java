package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.gamer.Gamer;
import fit.networks.protocol.SnakesProto;

import java.util.concurrent.atomic.AtomicInteger;

public class MessageBuilder {
    private static AtomicInteger messageSeq = new AtomicInteger(0);

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
        steerMessage.setDirection(direction.makeProtoDirection());
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


    public static SnakesProto.GamePlayer makeGamePlayer(Gamer gamer){
        SnakesProto.GamePlayer.Builder gamePlayer = SnakesProto.GamePlayer.newBuilder();
        gamePlayer.setName(gamer.getName());
        gamePlayer.setId(gamer.getId());
        gamePlayer.setIpAddress(gamer.getIpAddress().getHostName()); //TODO: check it
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

    public static SnakesProto.GameConfig makeGameConfig(GameConfig gameConfig){
        SnakesProto.GameConfig.Builder gameConfigProto = SnakesProto.GameConfig.newBuilder();
        gameConfigProto.setWidth(gameConfig.getWidth());
        gameConfigProto.setHeight(gameConfig.getHeight());
        gameConfigProto.setFoodStatic(gameConfig.getFoodStatic());
        gameConfigProto.setFoodPerPlayer((float)gameConfig.getFoodPerPlayer());
        gameConfigProto.setStateDelayMs(gameConfig.getDelayMs());
        gameConfigProto.setDeadFoodProb((float)gameConfig.getDeadFoodProb());
        return gameConfigProto.build();
    }

    public static SnakesProto.GameMessage makeAnnouncementMessage(Game game){
        SnakesProto.GameMessage.AnnouncementMsg.Builder announcementMsg = SnakesProto.GameMessage.AnnouncementMsg.newBuilder();
        SnakesProto.GameMessage.Builder msg = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GamePlayers.Builder gamePlayers = SnakesProto.GamePlayers.newBuilder();

        for (Gamer gamer : game.getActiveGamers()){
            gamePlayers.addPlayers(makeGamePlayer(gamer));
        }

        announcementMsg.setPlayers(gamePlayers);
        announcementMsg.setConfig(makeGameConfig(game.getGameConfig()));

        msg.setMsgSeq(messageSeq.getAndAdd(1));
        msg.setAnnouncement(announcementMsg);
        return msg.build();
    }

}
