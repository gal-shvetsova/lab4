package fit.networks.game.snake;

import fit.networks.game.Gamer;
import java.util.Iterator;

public class Snake implements Iterable<Coordinates> {
    private final int width;
    private final int height;
    private final Gamer gamer;
    private Node start;
    private Node finish;
    private Direction direction;
    private boolean isAlive;


    private class SnakeIterator implements Iterator<Coordinates> {
        private Node current = start;

        @Override
        public boolean hasNext() {

            return current != null;
        }

        @Override
        public Coordinates next() {
            Coordinates coordinates = current.coordinates;
            current = current.next;
            return coordinates;
        }
    }

    @Override
    public Iterator<Coordinates> iterator() {
        return new SnakeIterator();
    }


    public void run() {
        switch (direction) {
            case UP: {
                if (start.coordinates.getY() - 1 >= 0)
                    move(new Node(start.coordinates.getX(), start.coordinates.getY() - 1));
                else isAlive = false;
                break;
            }
            case DOWN: {
                if (start.coordinates.getY() + 1 <= height)
                    move(new Node(start.coordinates.getX(), start.coordinates.getY() + 1));
                else isAlive = false;
                break;
            }
            case RIGHT: {
                if (start.coordinates.getX() + 1 <= width)
                    move(new Node(start.coordinates.getX() + 1, start.coordinates.getY()));
                else isAlive = false;
                break;
            }
            case LEFT: {
                if (start.coordinates.getX() - 1 >= 0)
                    move(new Node(start.coordinates.getX() - 1, start.coordinates.getY()));
                else isAlive = false;
                break;
            }
        }
    }

    public Snake(Gamer gamer, int width, int height) {
        this.gamer = gamer;
        this.width = width;
        this.height = height;
        this.isAlive = true;
    }

    private class Node {
        Node next;
        Node prev;
        Coordinates coordinates;

        public Node(int x, int y) {
            this.coordinates = new Coordinates(x, y);
        }
    }

    private void move(Node newStart) {
        start.prev = newStart;
        newStart.next = start;
        newStart.prev = null;
        finish.prev.next = null;
        finish = finish.prev;
        start = newStart;
    }

    public Gamer getGamer() {
        return gamer;
    }

    public boolean isAlive() {
        return isAlive;
    }


    public void setStartCoordinates(int x, int y) {
        start = new Node(x, y);
        finish = start;
        start.next = finish;
        start.prev = null;
        finish.prev = start;
        finish.next = null;
        direction = Direction.getRandomDirection();
    }

    public void randomStart() {
        int x = (int) (Math.random() * width);
        int y = (int) (Math.random() * height);
        System.out.println("x " + x + " y" + y);
        start = new Node(x, y);
        start.next = finish;
        start.prev = null;
        direction = Direction.getRandomDirection();
        switch (direction) {
            case LEFT:
                finish = new Node(x + 1, y);
                break;
            case DOWN:
                finish = new Node(x, y + 1);
                break;
            case RIGHT:
                finish = new Node(x - 1, y);
                break;
            case UP:
                finish = new Node(x, y - 1);
                break;
        }
        finish.prev = start;
        finish.next = null;

    }


    //TODO переместить это в таймертаск или сделать синхронизацию
    public void changeDirection(int x, int y) {
        Direction newDirection = Direction.getDirection(x, y);
        if (newDirection == direction) return;
        direction = newDirection;
    }

}
