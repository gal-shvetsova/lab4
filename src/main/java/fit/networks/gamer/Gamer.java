package fit.networks.gamer;

import fit.networks.game.Coordinates;
import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.gamefield.Field;
import fit.networks.game.snake.Direction;
import fit.networks.game.snake.Snake;

import java.awt.*;
import java.net.InetAddress;
import java.util.Deque;
import java.util.Random;
import java.util.logging.Logger;

public class Gamer {
    private static int nextId = 2;
    private String name;
    private int id;
    private InetAddress ipAddress;
    private int port;
    private Snake snake;
    private Color color;
    private Role role;
    private boolean isZombie = false;

    public Gamer(String name, InetAddress ipAddress, int port, GameConfig gameConfig, Role role) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        nextId+=2;  //todo: do something with that
        Random random = new Random();
        this.id = random.nextInt();
        this.snake = new Snake(gameConfig.getMaxCoordinates());
        this.role = role;
        Random rand = new Random();
/*        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        this.color = new Color(r, g, b);  //todo: make it depends by id*/
        color = Color.BLACK;
    }

    public Gamer(String name, InetAddress ipAddress, int port, GameConfig gameConfig, Role role, int id) {  //todo: make something
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        nextId+=2;  //todo: do something with that
        Random random = new Random();
        this.id = id;
        this.snake = new Snake(gameConfig.getMaxCoordinates());
        this.role = role;
        Random rand = new Random();
        color = Color.BLACK;
/*        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        this.color = new Color(r, g, b);*/
    }

    public Gamer(InetAddress inetAddress, int port) {
        this.ipAddress = inetAddress;
        this.port = port;
    }


    public static Gamer getNewGameMaster(String name, InetAddress inetAddress, int port, GameConfig gameConfig) {
        nextId = 2;
        return new Gamer(name, inetAddress, port, gameConfig, Role.MASTER);
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
        return snake.getPoints();
    }

    public Color getColor() {
        return color;
    }

    public Snake getSnake() {
        return snake;
    }


    public Coordinates getSnakeHeadCoordinates() {
        return snake.getCoordinates().getFirst();
    }

    public boolean isHead(Coordinates coordinates){
        return getSnakeHeadCoordinates().equals(coordinates);
    }

    public Deque<Coordinates> getSnakeCoordinates() {
        return snake.getCoordinates();
    }

    public void setSnake(Snake snake) {
        this.snake = snake;
    }


    @Override
    public boolean equals(Object gamer) {
        if (!(gamer instanceof Gamer)) {
            return false;
        }
        return (((Gamer) gamer).port == port && ((Gamer) gamer).ipAddress == ipAddress);
    }

    public boolean isDead() {
        return snake.getKeyPoints().isEmpty();
    }

    public boolean isMaster() {
        return role == Role.MASTER;
    }

    public boolean isDeputy() {
        return role == Role.DEPUTY;
    }

    public boolean isViewer(){
        return role == Role.VIEWER;
    }

    public void becomeDying() {
        snake.setDyingState();
    }

    public void makeStep() {
        snake.run();
    }

    public void moveSnake(Direction direction) {
        snake.changeDirection(direction);
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
