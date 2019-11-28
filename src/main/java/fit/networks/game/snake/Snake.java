package fit.networks.game.snake;

import fit.networks.game.Coordinates;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.logging.Logger;

public class Snake {
    private final Coordinates maxCoordinates;
    private Deque<Coordinates> keyPoints = new ArrayDeque<>();
    private Direction direction;
    private Direction newDirection;
    private boolean isGrowing = false;
    private Logger logger;

    public Snake(Coordinates maxCoordinates) {
        this.maxCoordinates = maxCoordinates;
        logger = Logger.getLogger("snake");
    }

    synchronized public Direction getDirection() {
        return direction;
    }

    synchronized public void run() {
        if (isAlive()) {
            Coordinates oldHead = keyPoints.pollFirst();
            Coordinates newHead = oldHead.move(newDirection);


            if (direction == newDirection) {
                Coordinates node = keyPoints.pollFirst();
                node = Coordinates.of((int) (node.getX() + Math.signum(node.getX()))
                        , (int) (node.getY() + Math.signum(node.getY())));
                keyPoints.addFirst(node);

                if (!isGrowing) {
                    Coordinates tail = keyPoints.pollLast();
                    Coordinates newTail = Coordinates.of((int) (tail.getX() - Math.signum(tail.getX())), (int) (tail.getY() - Math.signum(tail.getY())));
                    if (!newTail.equals(Coordinates.of(0, 0))) {
                        keyPoints.addLast(newTail);
                    }
                } else {
                    isGrowing = false;
                }
                keyPoints.addFirst(circuitCoordinates(newHead));
                logger.info("old dir " + keyPoints.toString());
                return;
            }

            Coordinates newShift = oldHead.subtraction(newHead);
            keyPoints.addFirst(newShift);
            keyPoints.addFirst(circuitCoordinates(newHead));
            if (!isGrowing) {
                Coordinates tail = keyPoints.pollLast();
                Coordinates newTail = Coordinates.of((int)(tail.getX() - Math.signum(tail.getX())), (int)(tail.getY() - Math.signum(tail.getY())));
                if (!newTail.equals(Coordinates.of(0, 0))) {
                    keyPoints.addLast(newTail);
                }
                direction = newDirection;

            } else {
                isGrowing = false;
            }
        }

        logger.info(keyPoints.toString());
    }

    synchronized public boolean isAlive() {
        return !keyPoints.isEmpty();
    }


    synchronized public void die() {
        keyPoints.clear();
    }

    synchronized public void grow() {
        isGrowing = true;
    }


    synchronized public void setStartCoordinates(int x, int y) {
        Coordinates head = Coordinates.of(x, y);
        keyPoints.addFirst(head);
        direction = Direction.getRandomDirection();
        newDirection = direction;
        Coordinates tail = head.move(direction);

        keyPoints.addLast(tail.subtraction(head));
    }

    synchronized public void randomStart() {
        Random random = new Random();
        int x = random.nextInt(maxCoordinates.getX());
        int y = random.nextInt(maxCoordinates.getY());
        setStartCoordinates(x, y);
        System.out.println("x " + x + " y " + y);
    }

    private Coordinates circuitCoordinates(Coordinates nextStep) {
        logger.info("start " + nextStep);
        int x = nextStep.getX(), y = nextStep.getY();
        if (x < 0) {
            x = maxCoordinates.getX() + x;
        }
        if (y < 0) {
            y = maxCoordinates.getY() + y;
        }
        if (x >= maxCoordinates.getX()) {
            x = x %maxCoordinates.getX() ;
        }
        if (y >= maxCoordinates.getY()) {
            y = y % maxCoordinates.getY();
        }
        logger.info("finish " + x + " " + y);
        return Coordinates.of(x, y);
    }

    synchronized public Deque<Coordinates> getKeyPoints() {
        return keyPoints;
    }

    synchronized public Deque<Coordinates> getCoordinates() {
        Deque<Coordinates> coordinates = new ArrayDeque<>();
        int lastX = keyPoints.peekFirst().getX(), lastY = keyPoints.peekFirst().getY();

        coordinates.addFirst(Coordinates.of(lastX, lastY));
        for (Coordinates c : keyPoints) {
            if (c.getX() == lastX && c.getY() == lastY) continue;
            for (int i = 0; i < Math.abs(c.getX()); i++) {
                lastX += Math.signum(c.getX());
                coordinates.addLast(circuitCoordinates(Coordinates.of(lastX, lastY)));
            }
            for (int i = 0; i < Math.abs(c.getY()); i++) {
                lastY += Math.signum(c.getY());
                coordinates.addLast(circuitCoordinates(Coordinates.of(lastX, lastY)));
            }

        }
        logger.info("coord " + coordinates.toString());
        return coordinates;
    }

    synchronized public void changeDirection(int x, int y) {
        Direction newDirection = Direction.getDirection(x, y);
        if (newDirection == direction) return;
        if (direction.isOpposite(newDirection)) return;
        this.newDirection = newDirection;
    }

}
