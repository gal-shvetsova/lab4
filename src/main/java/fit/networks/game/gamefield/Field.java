package fit.networks.game.gamefield;

import fit.networks.game.Coordinates;

import java.awt.*;
import java.util.Deque;

public class Field {
    private Cell[][] field;
    private Coordinates maxCoordinates;

    public void setCells(Deque<Coordinates> coordinates, Cell newValue){
        for (Coordinates c : coordinates) {
            field[c.getX()][c.getY()] = newValue;
        }
    }

    public void setCells(Coordinates coordinates, Cell newValue){
        field[coordinates.getX()][coordinates.getY()] = newValue;
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

    public int getValue(Coordinates c){
        return field[c.getX()][c.getY()].getValue();
    }

    public Field(Coordinates maxCoordinates, Cell cell) {
        this.maxCoordinates = maxCoordinates;
        field = new Cell[maxCoordinates.getX()][maxCoordinates.getY()];
        for (int i = 0; i < maxCoordinates.getX(); i++) {
            for (int j = 0; j < maxCoordinates.getY(); j++)
                field[i][j] = cell;
        }
    }

}
