package fit.networks.game;

import java.net.InetAddress;
import java.util.ArrayList;

public class Game {
    private GameConfig gameConfig = new GameConfig();
    private ArrayList<Gamer> activeGamers = new ArrayList<>();

    public Game(String name, InetAddress ipAddress, int port) throws Exception{
        activeGamers.add(new Gamer(name, ipAddress, port));
        gameConfig = new GameConfig();
    }

    public Game(Gamer gamer, int width, int height, int foodStatic, float foodPerPlayer, int delayMs,
                float deadFoodProb) throws Exception{
        activeGamers.add(gamer);
        gameConfig.setWidth(width);
        gameConfig.setHeight(height);
        gameConfig.setFoodStatic(foodStatic);
        gameConfig.setFoodPerPlayer(foodPerPlayer);
        gameConfig.setDelayMs(delayMs);
        gameConfig.setDeadFoodProb(deadFoodProb);
    }

    public void treatPingMessage(InetAddress inetAddress, int port) {
    }
}
