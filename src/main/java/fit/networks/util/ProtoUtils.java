package fit.networks.util;

import fit.networks.protocol.SnakesProto;

public class ProtoUtils {
    public static SnakesProto.Direction get(fit.networks.game.snake.Direction direction) {
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
}
