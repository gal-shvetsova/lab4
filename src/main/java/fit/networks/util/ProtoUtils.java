package fit.networks.util;

import fit.networks.game.Coordinates;
import fit.networks.game.snake.Direction;
import fit.networks.game.snake.State;
import fit.networks.gamer.Role;
import fit.networks.protocol.SnakesProto;

public class ProtoUtils {
    public static SnakesProto.Direction getProtoDirection(fit.networks.game.snake.Direction direction) {
        switch (direction) {
            case UP:
                return SnakesProto.Direction.UP;
            case DOWN:
                return SnakesProto.Direction.DOWN;
            case LEFT:
                return SnakesProto.Direction.LEFT;
            case RIGHT:
                return SnakesProto.Direction.RIGHT;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public static SnakesProto.NodeRole getProtoRole(Role role) {
        switch (role) {
            case NORMAL:
                return SnakesProto.NodeRole.NORMAL;
            case MASTER:
                return SnakesProto.NodeRole.MASTER;
            case DEPUTY:
                return SnakesProto.NodeRole.DEPUTY;
            case VIEWER:
                return SnakesProto.NodeRole.VIEWER;

            default:
                throw new IllegalStateException("Unexpected value: " + role);
        }
    }

    public static Role getRole(SnakesProto.NodeRole role){
        switch (role){
            case NORMAL:
                return Role.NORMAL;
            case MASTER:
                return Role.MASTER;
            case DEPUTY:
                return Role.DEPUTY;
            case VIEWER:
                return Role.VIEWER;
            default:
                throw new IllegalStateException("Unexpected value: " + role);
        }
    }

    public static Direction getDirection(SnakesProto.Direction direction){
        switch (direction){
            case UP:
               return Direction.UP;
            case DOWN:
                return Direction.DOWN;
            case LEFT:
                return Direction.LEFT;
            case RIGHT:
                return Direction.RIGHT;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public static Coordinates getCoordinates(SnakesProto.GameState.Coord c){
        return Coordinates.of(c.getX(), c.getY());
    }

    public static SnakesProto.GameState.Snake.SnakeState getProtoState(State state) {
        switch (state){
            case ALIVE:
                return SnakesProto.GameState.Snake.SnakeState.ALIVE;
            case ZOMBIE:
                return SnakesProto.GameState.Snake.SnakeState.ZOMBIE;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }

    public static State getState(SnakesProto.GameState.Snake.SnakeState state){
        switch (state){
            case ALIVE:
                return State.ALIVE;
            case ZOMBIE:
                return State.ZOMBIE;
            default:
                throw new IllegalStateException("Unexpected value: " + state);
        }
    }
}
