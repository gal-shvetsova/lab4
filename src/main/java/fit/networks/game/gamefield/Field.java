package fit.networks.game.gamefield;

import fit.networks.gamer.Gamer;
import fit.networks.game.Coordinates;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Field {
    private Cell[][] field;
    private Coordinates maxCoordinates;

    public enum Result {
        OK,
        DIE,
        GROW;

    }
    public int getWidth(){
        return maxCoordinates.getX();
    }

    public int getHeight(){
        return maxCoordinates.getY();
    }

    public Color getColor(int i, int j) {
        return field[i][j].getColor();
    }

    public boolean isEmpty(int x, int y){
        return field[x][y].isEmpty();
    }

    public Field(Coordinates maxCoordinates) {
        this.maxCoordinates = maxCoordinates;
        field = new Cell[maxCoordinates.getX()][maxCoordinates.getY()];
        for (int i = 0; i < maxCoordinates.getX(); i++) {
            for (int j = 0; j < maxCoordinates.getY(); j++)
                field[i][j] = new Cell();
        }
    }

    public void addFoods(ArrayList<Coordinates> foods) {
        for (Coordinates food : foods) {
            field[food.getX()][food.getY()].setFood();
        }
    }

    public ArrayList<Coordinates> generateFoods(int neededFoodCount) {
        ArrayList<Coordinates> foods = new ArrayList<>();
        int width = maxCoordinates.getX();
        int height = maxCoordinates.getY();

        while (foods.size() < neededFoodCount) {
            Coordinates newFoods = Coordinates.getRandomCoordinates(width, height);
            while (!field[newFoods.getX()][newFoods.getY()].isEmpty())
                newFoods = Coordinates.getRandomCoordinates(width, height);
            foods.add(newFoods);
            field[newFoods.getX()][newFoods.getY()].setFood();
        }
        return foods;
    }

    public ArrayList<Coordinates> getFoodsAfterDie(Gamer gamer, double probability) {
        ArrayList<Coordinates> foods = new ArrayList<>();
        for (Coordinates c : gamer.getSnakeCoordinates()) {
            Random random = new Random();
            int value = random.nextInt(101);
            if (value < probability * 100) {
                foods.add(Coordinates.of(c.getX(), c.getY()));
                field[c.getX()][c.getY()].setFood();
            }
        }
        return foods;
    }

    public Result addGamerSnake(Gamer gamer) {
        if (gamer.isZombie()) return Result.OK;
        Result result = Result.OK;
        for (Coordinates c : gamer.getSnakeCoordinates()) {
            if (field[c.getX()][c.getY()].isFood()) {
                result = Result.GROW;
            } else if (field[c.getX()][c.getY()].isUser()) {
                result = Result.DIE;
                break;
            } else {
                field[c.getX()][c.getY()].setUser(gamer);
            }
        }
        return result;
    }
}
