package fit.networks.view;

import fit.networks.game.GameConfig;
import fit.networks.game.gamefield.Field;

public interface View {
    void showDeadForm();
    void loadNewField(Field field);
    void loadAvailableGames(String[][] games);
    void endGame();
    void showForm();
    void startGame(GameConfig gameConfig);
}
