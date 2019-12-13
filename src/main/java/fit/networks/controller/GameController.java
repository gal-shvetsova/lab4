package fit.networks.controller;

import fit.networks.game.Game;
import fit.networks.game.GameConfig;
import fit.networks.game.snake.Direction;
import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;
import java.util.Optional;

public interface GameController {
    void startNewGame(GameConfig gameConfig);
    void keyActivity(int x, int y);
    String getName();
    void start();
    void addAvailableGame(InetAddress inetAddress, int port, SnakesProto.GameMessage.AnnouncementMsg message);
    void addAliveGamer(InetAddress inetAddress, int port);

    void joinGame(String addressStr, int parseInt);
    void hostGame(String name, InetAddress address, int port);

    void setGame(Game game);

    void loadNewState();

    void changeSnakeDirection(InetAddress inetAddress, int port, Direction direction);

    void becomeMaster();

    void becomeDeputy();

    Optional<Game> getGame();

    void requestViewing();

    void becomeViewer(InetAddress inetAddress, int port);
}

