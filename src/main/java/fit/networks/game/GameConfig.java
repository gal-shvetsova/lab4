package fit.networks.game;

public class GameConfig {

    private int width;           // Ширина поля в клетках (от 10 до 100)
    private int height;          // Высота поля в клетках (от 10 до 100)
    private int foodStatic;      // Количество клеток с едой, независимо от числа игроков (от 0 до 100)
    private double foodPerPlayer;  // Количество клеток с едой, на каждого игрока (вещественный коэффициент от 0 до 100)
    private int delayMs;      // Задержка между ходами (сменой состояний) в игре, в миллисекундах
    private double deadFoodProb; // Вероятность превращения мёртвой клетки в еду (от 0 до 1).

    public GameConfig(int width, int height, int foodStatic, float foodPerPlayer, int delayMs, float deadFoodProb) {
        this.width = width;
        this.height = height;
        this.foodStatic = foodStatic;
        this.foodPerPlayer = foodPerPlayer;
        this.delayMs = delayMs;
        this.deadFoodProb = deadFoodProb;
    }

    public GameConfig() {
        width = 40;
        height = 30;
        foodStatic = 1;
        foodPerPlayer = 1;
        delayMs = 1000;
        deadFoodProb = 0.1;
    }


    public int getWidth() {
        return width;
    }

    public void setWidth(int width) throws Exception {
        if (width < 10 || width > 100)
            throw new Exception("Incorrect width [" + width + "]. " +
                    "Width should be between 10 and 100");
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) throws Exception{
        if (height < 10 || height > 100)
            throw new Exception("Incorrect height [" + height + "]. " +
                    "Height should be between 10 and 100");
        this.height = height;
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public void setFoodStatic(int foodStatic) throws Exception {
        if (foodStatic < 10 || foodStatic > 100)
            throw new Exception("Incorrect food static value [" + foodStatic + "]. " +
                    "Food static value should be between 10 and 100");
        this.foodStatic = foodStatic;
    }

    public double getFoodPerPlayer() {
        return foodPerPlayer;
    }

    public void setFoodPerPlayer(double foodPerPlayer) throws Exception{
        if (foodPerPlayer < 10 || foodPerPlayer > 100)
            throw new Exception("Incorrect food per player value [" + foodPerPlayer + "]. " +
                    "Food per player value should be between 10 and 100");
        this.foodPerPlayer = foodPerPlayer;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) throws Exception{
        if (delayMs < 0)
            throw new Exception("Incorrect delay value [" + delayMs + "]. " +
                    "Delay value should be positive");
        this.delayMs = delayMs;
    }

    public double getDeadFoodProb() {
        return deadFoodProb;
    }

    public void setDeadFoodProb(double deadFoodProb) throws Exception{
        if (deadFoodProb < 0 || deadFoodProb > 1)
            throw new Exception("Incorrect dead food prob value [" + deadFoodProb + "]. " +
                    "Dead food prob value should be between 0 and 1");
        this.deadFoodProb = deadFoodProb;
    }
}
