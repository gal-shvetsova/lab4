package fit.networks.game;

public class GameConfig {

    private Coordinates maxCoordinates;        // Высота поля в клетках (от 10 до 100)

    private int foodStatic;      // Количество клеток с едой, независимо от числа игроков (от 0 до 100)
    private double foodPerPlayer;  // Количество клеток с едой, на каждого игрока (вещественный коэффициент от 0 до 100)
    private int delayMs;      // Задержка между ходами (сменой состояний) в игре, в миллисекундах
    private double deadFoodProb; // Вероятность превращения мёртвой клетки в еду (от 0 до 1).

    public GameConfig(int width, int height, int foodStatic, double foodPerPlayer, int delayMs, double deadFoodProb)
            throws IllegalArgumentException{
        maxCoordinates = new Coordinates();
        setWidth(width);
        setHeight(height);
        setFoodStatic(foodStatic);
        setFoodPerPlayer(foodPerPlayer);
        setDelayMs(delayMs);
        setDeadFoodProb(deadFoodProb);
    }

    public GameConfig() {
        this.maxCoordinates = new Coordinates(40, 30);
        this.foodStatic = 1;
        this.foodPerPlayer = 1;
        this.delayMs = 1000;
        this.deadFoodProb = 0.1;
    }


    public Coordinates getMaxCoordinates() {
        return maxCoordinates;
    }

    public int getWidth() {
        return maxCoordinates.getX();
    }

    public void setWidth(int width) throws IllegalArgumentException {
        if (width < 10 || width > 100)
            throw new IllegalArgumentException("Incorrect width [" + width + "]. " +
                    "Width should be between 10 and 100");
        this.maxCoordinates.setX(width);
    }

    public int getHeight() {
        return maxCoordinates.getY();
    }

    public void setHeight(int height) throws IllegalArgumentException{
        if (height < 10 || height > 100)
            throw new IllegalArgumentException("Incorrect height [" + height + "]. " +
                    "Height should be between 10 and 100");
        this.maxCoordinates.setY(height);
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public void setFoodStatic(int foodStatic) throws IllegalArgumentException {
        if (foodStatic < 10 || foodStatic > 100)
            throw new IllegalArgumentException("Incorrect food static value [" + foodStatic + "]. " +
                    "Food static value should be between 10 and 100");
        this.foodStatic = foodStatic;
    }

    public double getFoodPerPlayer() {
        return foodPerPlayer;
    }

    public void setFoodPerPlayer(double foodPerPlayer) throws IllegalArgumentException{
        if (foodPerPlayer < 0 || foodPerPlayer > 100)
            throw new IllegalArgumentException("Incorrect food per player value [" + foodPerPlayer + "]. " +
                    "Food per player value should be between 10 and 100");
        this.foodPerPlayer = foodPerPlayer;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) throws IllegalArgumentException{
        if (delayMs < 0)
            throw new IllegalArgumentException("Incorrect delay value [" + delayMs + "]. " +
                    "Delay value should be positive");
        this.delayMs = delayMs;
    }

    public double getDeadFoodProb() {
        return deadFoodProb;
    }

    public void setDeadFoodProb(double deadFoodProb) throws IllegalArgumentException{
        if (deadFoodProb < 0 || deadFoodProb > 1)
            throw new IllegalArgumentException("Incorrect dead food prob value [" + deadFoodProb + "]. " +
                    "Dead food prob value should be between 0 and 1");
        this.deadFoodProb = deadFoodProb;
    }
}
