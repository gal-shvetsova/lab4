package fit.networks.game.snake;

public enum Direction {
    LEFT(1),
    RIGHT(2),
    UP(3),
    DOWN(4);

    private final int index;

    Direction(int index){
        this.index = index;
    }

    int getValue(){
        return index;
    }

    public static Direction directionOf(int index){
        switch (index){
            case 1: return LEFT;
            case 2: return RIGHT;
            case 3: return UP;
            case 4: return DOWN;
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
