package fit.networks.game.snake;

import fit.networks.protocol.SnakesProto;

public enum Direction {
    UP(1),
    DOWN(2),
    LEFT(3),
    RIGHT(4);

    private final int index;

    Direction(int index){
        this.index = index;
    }

    int getValue(){
        return index;
    }

    public static Direction directionOf(int index){
        switch (index){
            case 1: return UP;
            case 2: return DOWN;
            case 3: return LEFT;
            case 4: return RIGHT;
        }
        return null;
    }

    public static Direction directionOf(SnakesProto.Direction direction){
        switch (direction.getNumber()){
            case 1: return UP;
            case 2: return DOWN;
            case 3: return LEFT;
            case 4: return RIGHT;
        }
        return null;
    }

    public SnakesProto.Direction makeProtoDirection(){
        switch (getValue()){
            case 1: return SnakesProto.Direction.UP;
            case 2: return SnakesProto.Direction.DOWN;
            case 3: return SnakesProto.Direction.LEFT;
            case 4: return SnakesProto.Direction.RIGHT;
        }
        return null;
    }

    public static Direction getDirection(int x, int y){
        switch (x){
            case -1:
                return LEFT;
            case 1:
                return RIGHT;
        }
        switch (y){
            case -1:
                return UP;
            case 1:
                return DOWN ;
        }
        return null;
    }

    public boolean isOpposite(Direction direction){
        if (this == LEFT)
            return direction == RIGHT;
        if (this == RIGHT)
            return direction == LEFT;
        if (this == UP)
            return direction == DOWN;
        if (this == DOWN)
            return direction== UP;
        return false;
    }

    public static Direction getRandomDirection(){
        return directionOf(LEFT.getValue() + (int) (Math.random() * DOWN.getValue()));
    }
}
