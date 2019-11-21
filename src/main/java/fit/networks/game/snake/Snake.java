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
        if (outOfField(nextStep)) {
            die();
        }
        if (isAlive()) {
            if (!isGrowing) {
                coordinates.removeLast();
            } else isGrowing = false;
            coordinates.addFirst(nextStep);
        }
    }

    private boolean outOfField(Coordinates nextStep) {
        return nextStep.getY() >= maxCoordinates.getY() ||
                nextStep.getY() < 0 ||
                nextStep.getX() >= maxCoordinates.getX() ||
                nextStep.getX() < 0;
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
