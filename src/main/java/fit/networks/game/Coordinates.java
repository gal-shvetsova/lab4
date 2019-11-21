package fit.networks.game;

import fit.networks.game.snake.Direction;
import fit.networks.protocol.SnakesProto;

import java.util.Random;

public class Coordinates {
    private int x;
    private int y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Coordinates(){
        this.x = 0;
        this.y = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCoordinates(int x, int y){
        this.x = x;
        this.y = y;
    }

    public static Coordinates getRandomCoordinates(int maxX, int maxY){
        Random random = new Random();
        int x = random.nextInt(maxX);
        int y = random.nextInt(maxY);
        return new Coordinates(x, y);

    }

    public Coordinates move(Direction direction) {
        switch (direction) {
            case UP:
                return new Coordinates(this.x, this.y - 1);
            case DOWN:
                return new Coordinates(this.x, this.y + 1);
            case LEFT:
                return new Coordinates(this.x - 1, this.y);
            case RIGHT:
                return new Coordinates(this.x + 1, this.y);
            default:
                throw new IllegalArgumentException("Unrecognized direction type" + direction);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return x == ((Coordinates)obj).getX() && y == ((Coordinates)obj).getY();
    }

}
