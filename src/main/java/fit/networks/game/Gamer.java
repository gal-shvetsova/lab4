package fit.networks.game;

import fit.networks.protocol.SnakesProto;
import fit.networks.snake.Snake;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Gamer {
    private String name;       // Имя игрока (для отображения в интерфейсе)
    private UUID id;          // Уникальный идентификатор игрока в пределах игры
    private InetAddress ipAddress; // IPv4 или IPv6 адрес игрока в виде строки
    private int port;
    private Snake snake;

    public Gamer(String name, InetAddress ipAddress, int port) {
        this.name = name;
        this.id = UUID.randomUUID();
        this.ipAddress = ipAddress;
        this.port = port;
        this.snake = null;
    }


    public String getName() {
        return name;
    }

    public UUID getId() {
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
