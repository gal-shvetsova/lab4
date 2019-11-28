package fit.networks.controller;

import fit.networks.game.GameConfig;
import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;

public interface GameController {
    void startNewGame(GameConfig gameConfig);
    void keyActivity(int x, int y);
    String getName();
    void leaveGame();
    void start();
    void addAvailableGame(InetAddress inetAddress, int port, SnakesProto.GameMessage.AnnouncementMsg message);
    void addAliveGamer(InetAddress inetAddress, int port);

    void joinGame(String addressStr, int parseInt);
}

