package fit.networks.game;

import java.util.Random;

public class Coordinates {
    private int x;
    private int y;

    public Coordinates(int x, int y) {
        this.x = x;
        this.y = y;
    }

    Coordinates(){
        this.x = 0;
        this.y = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setCoordinates(int x, int y){
        this.x = x;
        this.y = y;
    }

    public static Coordinates getRandomCoordinates(int maxX, int maxY){
        Random random = new Random(System.currentTimeMillis());
        int x = random.nextInt(maxX);
        int y = random.nextInt(maxY);
        return new Coordinates(x, y);

    }

    @Override
    public boolean equals(Object obj) {
        return x == ((Coordinates)obj).getX() && y == ((Coordinates)obj).getY();
    }

}
