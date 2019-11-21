package fit.networks.gamer;

import fit.networks.game.Coordinates;
import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.gamefield.Field;
import fit.networks.game.snake.Snake;

import java.awt.*;
import java.net.InetAddress;
import java.util.ArrayDeque;
import java.util.Random;

public class Gamer {
    private static int nextId = 2;
    private String name;
    private int id;
    private InetAddress ipAddress;
    private int port;
    private Snake snake;
    private Color color;
    private boolean isMaster;
    private Game game;

    public Gamer(String name, InetAddress ipAddress, int port, GameConfig gameConfig, boolean isMaster) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.id = nextId++;
        this.snake = new Snake(gameConfig.getMaxCoordinates());
        this.isMaster = isMaster;
        this.game = new Game(gameConfig);
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        this.color = new Color(r, g, b);
    }

    public Gamer(InetAddress inetAddress, int port) {
        this.ipAddress = inetAddress;
        this.port = port;
        this.isMaster = false;
    }


    public static Gamer getNewGameMaster(String name, InetAddress inetAddress, int port, GameConfig gameConfig) {
        nextId = 2;
        return new Gamer(name, inetAddress, port, gameConfig, true);
    }


    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getPoints() {
        return snake.getCoordinates().size();
    }

    public Color getColor() {
        return color;
    }

    public Snake getSnake() {
        return snake;
    }

    public Game getGame() {
        return game;
    }

    public Coordinates getSnakeHeadCoordinates() {
        return snake.getCoordinates().peekFirst();
    }

    public ArrayDeque<Coordinates> getSnakeCoordinates() {
        return snake.getCoordinates();
    }

    public void setSnake(Snake snake) {
        this.snake = snake;
    }

    @Override
    public boolean equals(Object gamer) {
        return (((Gamer) gamer).port == port && ((Gamer) gamer).ipAddress == ipAddress);
    }

    public boolean isZombie() {
        return !snake.isAlive();
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void becomeZombie() {
        snake.die();
    }

    public void makeStep() {
        snake.run();
    }

    public void startNewGame() {
        game.addGamer(this);
        snake.randomStart();
    }

    public void moveSnake(int x, int y) {
        snake.changeDirection(x, y);
    }

    public Field getRepresentation() {
        return game.makeRepresentation();
    }
}
