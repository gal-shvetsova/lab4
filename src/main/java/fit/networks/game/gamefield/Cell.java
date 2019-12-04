package fit.networks.game.gamefield;

import java.awt.*;

public class Cell {
    private int value;
    private Color color;

    public Cell(int value, Color color) {
        this.value = value;
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public Color getColor() {
        return color;
    }
}
