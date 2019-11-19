package fit.networks.game;

import java.awt.*;

public class Cell {
    private int value;
    private Color color;

    public Cell(){
        value = 0;
        color = Color.WHITE;
    }

    void setValue(int value){
        this.value = value;
    }

    void setColor(Color color){
        this.color = color;
    }

    public int getValue(){
        return value;
    }

    public Color getColor(){
        return color;
    }

}
