package fit.networks.controller;

import fit.networks.game.GameConfig;

public interface SnakeController {
    void startNewGame(GameConfig gameConfig);
    void keyActivity(int x, int y);
    String getName();
    void leaveGame();
    void start();
}
