package fit.networks.game.snake;

import fit.networks.game.Coordinates;

import java.util.ArrayDeque;
import java.util.Random;

public class Snake {
    private final Coordinates maxCoordinates;
    private final ArrayDeque<Coordinates> coordinates = new ArrayDeque<>();
    private Direction direction;
    private boolean isGrowing = false;

    public void run() {
        Coordinates head = coordinates.getFirst();
        Coordinates nextStep = head.move(direction);
        nextStep = outOfField(nextStep);

        if (isAlive()) {
            if (!isGrowing) {
                coordinates.removeLast();
            } else isGrowing = false;
            coordinates.addFirst(nextStep);
        }
    }

    private Coordinates outOfField(Coordinates nextStep) {
        int x = nextStep.getX(), y = nextStep.getY();
        if (x < 0) x = maxCoordinates.getX() - 1;
        if (y < 0) y = maxCoordinates.getY() - 1;
        if (x >= maxCoordinates.getX()) x = 0;
        if (y >= maxCoordinates.getY()) y = 0;
        return new Coordinates(x, y);
    }


    public void die() {
        coordinates.clear();
    }

    public Snake(Coordinates maxCoordinates) {
        this.maxCoordinates = maxCoordinates;
    }

    public void grow() {
        isGrowing = true;
    }

    public boolean isAlive() {
        return !coordinates.isEmpty();
    }


    public void setStartCoordinates(int x, int y) {
        Coordinates tail = new Coordinates(x, y);
        coordinates.addFirst(tail);
        direction = Direction.getRandomDirection();
        coordinates.addFirst(tail.move(direction));
    }

    public void randomStart() {
        Random random = new Random();
        int x = random.nextInt(maxCoordinates.getX());
        int y = random.nextInt(maxCoordinates.getY());
        setStartCoordinates(x, y);
        System.out.println("x " + x + " y " + y);
    }


    //TODO переместить это в таймертаск или сделать синхронизацию
    public void changeDirection(int x, int y) {
        Direction newDirection = Direction.getDirection(x, y);
        if (newDirection == direction) return;
        if (direction.isOpposite(newDirection)) return;
        direction = newDirection;
    }

    public ArrayDeque<Coordinates> getCoordinates() {
        return coordinates;
    }
}
