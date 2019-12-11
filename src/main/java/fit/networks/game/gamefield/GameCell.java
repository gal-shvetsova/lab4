package fit.networks.game.gamefield;

import fit.networks.protocol.Protocol;

import java.awt.*;

public class GameCell extends Cell {

    private GameCell(int value, Color color) {
        super(value, color);
    }

    public static GameCell getFoodCell(){
        return new GameCell(Protocol.getFoodValue(), Protocol.getFoodColor());
    }

    public static GameCell getNoneCell(){
        return new GameCell(Protocol.getNoneValue(), Protocol.getNoneColor());
    }

    public static GameCell getGamerCell(int value, Color color){
        return new GameCell(value, color);
    }

    public static boolean isSnake(Cell gameCell){
        return !isEmpty(gameCell) && !isFood(gameCell);
    }

    public static boolean isEmpty(Cell gameCell){
        return gameCell.getValue() == Protocol.getNoneValue();
    }

    public static boolean isFood(Cell gameCell){
       return gameCell.getValue() == Protocol.getFoodValue();
    }

    public static int getId(Cell gameCell){
        if (isSnake(gameCell)) {
            return gameCell.getValue();
        }
        return -1;
    }

}
