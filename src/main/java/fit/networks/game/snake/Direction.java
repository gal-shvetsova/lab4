package fit.networks.game.snake;

import java.util.Random;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

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

    public boolean isOpposite(Direction direction) {
        return this == getOpposite(direction);
    }

    public Direction getOpposite(Direction direction){
        switch (direction){
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                throw new IllegalStateException("Unexpected value: " + direction);
        }
    }

    public static Direction getRandomDirection(){
        return values()[new Random().nextInt(values().length)];
    }


    public Direction getOpposite() {
        return getOpposite(this);
    }
}
