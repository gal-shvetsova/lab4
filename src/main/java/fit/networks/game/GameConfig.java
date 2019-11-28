package fit.networks.game;

public class GameConfig {

    private static final int MAX_WIDTH = 100;
    private static final int MAX_HEIGHT = 100;
    private static final int MAX_FOOD_STATIC = 100;
    private static final double MAX_FOOD_PER_PLAYER = 100; //TODO: check usages

    private static final int MIN_WIDTH = 10;
    private static final int MIN_HEIGHT = 10;
    private static final int MIN_FOOD_STATIC = 0;
    private static final double MIN_FOOD_PER_PLAYER = 0;
    private static final int MIN_DELAY_MS = 100;
    private Coordinates maxCoordinates;        // Высота поля в клетках (от 10 до 100)

    private int foodStatic;      // Количество клеток с едой, независимо от числа игроков (от 0 до 100)
    private double foodPerPlayer;  // Количество клеток с едой, на каждого игрока (вещественный коэффициент от 0 до 100)
    private int delayMs;      // Задержка между ходами (сменой состояний) в игре, в миллисекундах
    private double deadFoodProb; // Вероятность превращения мёртвой клетки в еду (от 0 до 1).

    public GameConfig(int width, int height, int foodStatic, double foodPerPlayer, int delayMs, double deadFoodProb)
            throws IllegalArgumentException {

        validateArguments(
                width,
                height,
                foodStatic,
                foodPerPlayer,
                delayMs,
                deadFoodProb
        );

        this.maxCoordinates = Coordinates.of(width, height);
        this.foodStatic = foodStatic;
        this.foodPerPlayer = foodPerPlayer;
        this.delayMs = delayMs;
        this.deadFoodProb = deadFoodProb;

    }

    public Coordinates getMaxCoordinates() {
        return maxCoordinates;
    }

    public int getFoodStatic() {
        return foodStatic;
    }

    public double getFoodPerPlayer() {
        return foodPerPlayer;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public double getDeadFoodProb() {
        return deadFoodProb;
    }

    private void validateArguments(int width, int height, int foodStatic, double foodPerPlayer, int delayMs, double deadFoodProb) {
        if (width < 10 || width > 100) {
            throw new IllegalArgumentException("Incorrect width [" + width + "]. " +
                    "Width should be between " + MIN_WIDTH  + " and " + MAX_WIDTH);
        }
        if (height < 10 || height > 100) {
            throw new IllegalArgumentException("Incorrect height [" + height + "]. " +
                    "Height should be between " + MIN_HEIGHT + " and " + MAX_HEIGHT);
        }

        if (foodStatic < 10 || foodStatic > 100) {
            throw new IllegalArgumentException("Incorrect food static value [" + foodStatic + "]. " +
                    "Food static value should be between " + MIN_FOOD_STATIC + " and " + MAX_FOOD_STATIC);
        }

        if (foodPerPlayer < 0 || foodPerPlayer > 100) {
            throw new IllegalArgumentException("Incorrect food per player value [" + foodPerPlayer + "]. " +
                    "Food per player value should be between " + MIN_FOOD_PER_PLAYER + " and " + MAX_FOOD_PER_PLAYER);
        }
        if (delayMs < 0) {
            throw new IllegalArgumentException("Incorrect delay value [" + delayMs + "]. " +
                    "Delay value should be bigger than " + MIN_DELAY_MS);
        }
        if (deadFoodProb < 0 || deadFoodProb > 1)
            throw new IllegalArgumentException("Incorrect dead food prob value [" + deadFoodProb + "]. " +
                    "Dead food prob value should be between 0 and 1");

    }

    public GameConfig() {
        this.maxCoordinates = Coordinates.of(40, 30);
        this.foodStatic = 1;
        this.foodPerPlayer = 1;
        this.delayMs = 1000;
        this.deadFoodProb = 0.1;
    }

    public int getWidth() {
        return maxCoordinates.getX();
    }

    public int getHeight() {
        return maxCoordinates.getY();
    }
}
