package fit.networks.game;

import fit.networks.game.snake.Direction;
import fit.networks.protocol.SnakesProto;

import java.util.Random;

public class Coordinates {
    private final int x;
    private int y;

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    private Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Coordinates of(int x, int y){
        return new Coordinates(x,y);
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Coordinates subtraction(Coordinates c){
        int x = this.x - c.getX();
        int y = this.y - c.getY();
        return Coordinates.of(x,y);
    }

    public static Coordinates circuitCoordinates(Coordinates coordinatesToCircuit, Coordinates maxCoordinates) {
        int x = coordinatesToCircuit.getX(), y = coordinatesToCircuit.getY();
        int maxX = maxCoordinates.getX(), maxY = maxCoordinates.getY();

        if (x < 0) {
            x = maxX + x;
        }
        if (y < 0) {
            y = maxY + y;
        }
        if (x >= maxX) {
            x = x % maxX;
        }
        if (y >= maxY) {
            y = y % maxY;
        }
        return Coordinates.of(x, y);
    }

    public static Coordinates getRandomCoordinates(int maxX, int maxY){
        Random random = new Random();
        int x = random.nextInt(maxX);
        int y = random.nextInt(maxY);
        return Coordinates.of(x, y);

    }

    public Coordinates move(Direction direction) {
        switch (direction) {
            case UP:
                return Coordinates.of(this.x, this.y - 1);
            case DOWN:
                return Coordinates.of(this.x, this.y + 1);
            case LEFT:
                return Coordinates.of(this.x - 1, this.y);
            case RIGHT:
                return Coordinates.of(this.x + 1, this.y);
            default:
                throw new IllegalArgumentException("Unrecognized direction type" + direction);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coordinates)) return false;
        return x == ((Coordinates)obj).getX() && y == ((Coordinates)obj).getY();
    }

}
