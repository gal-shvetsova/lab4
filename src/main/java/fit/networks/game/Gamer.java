package fit.networks.game;

import fit.networks.game.snake.Snake;

import java.net.InetAddress;

public class Gamer {
    private String name;       // Имя игрока (для отображения в интерфейсе)
    private int id;          // Уникальный идентификатор игрока в пределах игры
    private InetAddress ipAddress; // IPv4 или IPv6 адрес игрока в виде строки
    private int port;
    private Snake snake;
    private boolean isMaster;

    public Gamer(String name, InetAddress ipAddress, int port, int id, boolean isMaster) {
        this.name = name;
        this.ipAddress = ipAddress;
        this.port = port;
        this.id = id;
        this.snake = null;
        this.isMaster = isMaster;
    }

    public Gamer(InetAddress inetAddress, int port){
        this.ipAddress = inetAddress;
        this.port = port;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void makeMaster(){
        isMaster = true;
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


    public Snake getSnake() {
        return snake;
    }

    public void setSnake(Snake snake) {
        this.snake = snake;
    }

    public boolean equals(Gamer gamer){
        return (gamer.port == port && gamer.ipAddress == ipAddress);
    }




}
