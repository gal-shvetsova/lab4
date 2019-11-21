package fit.networks.game;

import fit.networks.game.gamefield.Field;
import fit.networks.gamer.Gamer;

import java.util.ArrayList;

public class Game {  // создается только у мастера, сделать создание из предыдущей игры

    private GameConfig gameConfig;
    private ArrayList<Gamer> activeGamers = new ArrayList<>();
    private ArrayList<Gamer> activeGamersPerCycle = new ArrayList<>();
    private ArrayList<Coordinates> foods = new ArrayList<>();


    public Game(GameConfig gameConfig) {
        this.gameConfig = gameConfig;

    }

    public ArrayList<Gamer> getActiveGamers() {
        return activeGamers;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public void addGamer(Gamer gamer) {
        activeGamers.add(gamer);
    }

    public boolean hasAliveGamers(){
        for (Gamer gamer : activeGamers){
            if (!gamer.isZombie()) return true;
        }
        return false;
    }

    public Field makeRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates());
        int neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * activeGamers.size());
        field.addFoods(foods);

        for (Gamer gamer : activeGamers) {
            switch (field.addGamerSnake(gamer)) {
                case DIE: {
                    foods.addAll(field.getFoodsAfterDie(gamer, gameConfig.getDeadFoodProb()));
                    gamer.becomeZombie();
                    break;
                }
                case GROW: {
                    foods.remove(gamer.getSnakeHeadCoordinates());
                    gamer.getSnake().grow();
                    break;
                }
            }
        }

        foods.addAll(field.generateFoods(neededFoods - foods.size()));
        return field;
    }
}
