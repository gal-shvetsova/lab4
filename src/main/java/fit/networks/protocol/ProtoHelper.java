package fit.networks.protocol;

import fit.networks.game.Coordinates;
import fit.networks.game.snake.Snake;

import java.util.concurrent.atomic.AtomicInteger;

public class ProtoHelper {
    public static SnakesProto.GamePlayer getMasterId(SnakesProto.GamePlayers players){
        for (SnakesProto.GamePlayer player : players.getPlayersList()){
            if (player.getRole() == SnakesProto.NodeRole.MASTER){
                return player;
            }
        }
        return  null;
    }

}
