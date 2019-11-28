package fit.networks.protocol;

public class ProtoHelper {
    public static SnakesProto.GamePlayer getMaster(SnakesProto.GamePlayers players){
        for (SnakesProto.GamePlayer player : players.getPlayersList()){
            if (player.getRole() == SnakesProto.NodeRole.MASTER){
                return player;
            }
        }
        return  null;
    }

}
