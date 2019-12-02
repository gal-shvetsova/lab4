package fit.networks.game.gamefield;

import fit.networks.gamer.Gamer;

import java.awt.*;

public class Cell {
    private int value;
    private Color color;
    private static final  Color FOOD_COLOR = Color.red;
    private static final Color NONE_COLOR = Color.white;
    private static final int FOOD_VALUE = 1;
    private static final int NONE_VALUE = 0;

    public Cell(){
        value = NONE_VALUE;
        color = NONE_COLOR;
    }

    void setUser(Gamer gamer) {
        if (gamer.getId() < 2) return;
        this.value = gamer.getId();
        this.color = gamer.getColor();
    }

    void setFood() {
        this.value =  FOOD_VALUE;
        this.color = FOOD_COLOR;
    }

    void setEmpty(){
        this.value = NONE_VALUE;
        this.color = NONE_COLOR;
    }

    public boolean isFood(){
        return value == FOOD_VALUE;
    }

    public boolean isEmpty(){
        return value == NONE_VALUE;
    }

    public boolean isUser(){
        return !isEmpty() && !isFood();
    }

    public int getUserId(){
        return value; //todo throw something if no user
    }

    public Color getColor(){
        return color;
    }

}
