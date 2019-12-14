package fit.networks.gamer;

import fit.networks.game.Coordinates;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.game.snake.Snake;

import javax.annotation.Nullable;
import java.awt.*;
import java.net.InetAddress;
import java.util.Deque;

public class Gamer {
    private static int nextId = 3;
    private String name;
    private int id;
    private InetAddress ipAddress;
    private int port;
    private Snake snake;
    private Color color;
    private Role role;
    private boolean isZombie = false;
    private int score = 0;

    public Gamer(String name, InetAddress ipAddress, int port, GameConfig gameConfig, Role role, @Nullable Integer id,
                 @Nullable Integer score) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        nextId+=3;
        this.id = id == null ? nextId : 3;
        this.snake = new Snake(gameConfig.getMaxCoordinates());
        this.role = role;
        color = Color.BLACK;
        if (id != null && score != null) {
            this.id = id;
            this.score = score;
        }
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

    public int getScore(){
        return score;
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
        return (((Gamer) gamer).port == port && ((Gamer) gamer).ipAddress.equals(ipAddress));
    }

    public boolean isDead() {
        return snake.getKeyPoints().isEmpty();
    }

    public boolean isDying(){
        return snake.isDying();
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

    public void start(){ this.snake.randomStart();}

    public void start(Coordinates coordinates){
        snake.setStartCoordinates(coordinates);
    }

    public void addScore() {
        score++;
    }

    public Role getRole() {
        return role;
    }

}
