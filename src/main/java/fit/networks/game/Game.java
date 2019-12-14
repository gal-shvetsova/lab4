package fit.networks.game;

import fit.networks.game.gamefield.Field;
import fit.networks.game.gamefield.GameCell;
import fit.networks.game.gamefield.Square;
import fit.networks.gamer.Gamer;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Game {
    private final static Logger logger = Logger.getLogger("Game");
    private final GameConfig gameConfig;
    private Queue<Gamer> gamers = new ConcurrentLinkedQueue<>();
    private Queue<Pair<InetAddress, Integer>> activeGamersPerCycle = new ConcurrentLinkedQueue<>();
    private Deque<Coordinates> foods = new ArrayDeque<>();
    private AtomicInteger gameStateId = new AtomicInteger(0);
    private Field field;
    private List<Square> squares;

    public Game(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
        initSquares();
    }

    public Game(GameConfig gameConfig, Deque<Coordinates> foods) {
        this.gameConfig = gameConfig;
        this.foods = foods;
    }

    private void initSquares(){
        squares = new ArrayList<>();
        for (int x = 0; x < gameConfig.getWidth() - 5; x++) {
            for (int y = 0; y < gameConfig.getWidth() - 5; y++) {
                squares.add(new Square(Coordinates.of(x, y)));
            }
        }
    }

    public void updateAvailableCoordinates(){
        initSquares();
        for (int x = 0; x < gameConfig.getWidth(); x++) {
            for (int y = 0; y < gameConfig.getHeight(); y++) {
                Coordinates currentCoordinates = Coordinates.of(x, y);
                if (GameCell.isSnake(field.in(currentCoordinates))){
                    for (Square square : squares) {
                        if (square.contains(currentCoordinates)){
                            square.becomeUnavailable();
                        }
                    }
                }
            }
        }
    }

    public Optional<Coordinates> getAvailableCoordinates() {
        Optional<Square> available = squares.stream().filter(Square::isAvailable).findAny();

        if (available.isEmpty()){
            return Optional.empty();
        }

        return Optional.of(available.get().randomCoordinates());
    }

    public Optional<Gamer> getGamerByAddress(InetAddress inetAddress, int port) {
        return gamers.stream()
                .filter(x -> x.getIpAddress().equals(inetAddress))
                .filter(x -> x.getPort() == port).findFirst();
    }

    public Optional<Gamer> getGamerById(int id) {
        return gamers.stream()
                .filter(x -> x.getId() == id).findFirst();
    }

    public Optional<Gamer> getDeputy() {
        return gamers.stream().filter(Gamer::isDeputy).findFirst();

    }

    public boolean deputyAbsent() {
        return !gamers.stream().allMatch(Gamer::isDeputy);
    }

    public Optional<Gamer> getMaster() {
        return gamers.stream()
                .filter(Gamer::isMaster)
                .findFirst();
    }

    synchronized public Queue<Gamer> getAliveGamers() {
        return gamers;
    }

    public void addAliveGamer(InetAddress inetAddress, int port) {
        this.activeGamersPerCycle.add(Pair.of(inetAddress, port));
    }

    public void addGamer(Gamer gamer) {
        gamers.add(gamer);
    }

    public boolean hasAliveGamers() {
        return !gamers.stream()
                .allMatch(gamer -> gamer.isViewer() || gamer.isDead());
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public Deque<Coordinates> getFoodCoordinates() {
        return foods;
    }

    synchronized public Field makeMasterRepresentation() {
        field = new Field(gameConfig.getMaxCoordinates(), GameCell.getNoneCell());
        int neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        generateFood(neededFoods, field);
        drawFood();
        gamers.stream()
                .filter(gamer -> !gamer.isDying())
                .forEach(gamer -> gamer.getSnakeCoordinates()
                        .forEach(coordinates -> {
                            if (GameCell.isFood(field.in(coordinates)) && gamer.getSnake().isHead(coordinates)) {
                                gamer.getSnake().grow();
                                gamer.addScore();
                                drawSnakePoint(coordinates, gamer);
                                foods.remove(coordinates);
                            } else if (GameCell.isEmpty(field.in(coordinates))) {
                                drawSnakePoint(coordinates, gamer);
                            } else if (GameCell.isSnake(field.in(coordinates))) {
                                getGamerById(GameCell.getId(field.in(coordinates))).ifPresent(anotherGamer -> {
                                    if (anotherGamer.isHead(coordinates)) {
                                        anotherGamer.becomeDying();
                                    } else {
                                        anotherGamer.addScore();
                                    }
                                    if (gamer.isHead(coordinates)) {
                                        gamer.becomeDying();
                                    } else {
                                        gamer.addScore();
                                    }
                                });
                            }
                        }));

        getDying().forEach(this::makeFoodFromSnake);

        neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        generateFood(neededFoods, field);
        updateAvailableCoordinates();
        return field;
    }

    private void makeFoodFromSnake(Gamer gamer){
        for (Coordinates snakeCoordinate : gamer.getSnakeCoordinates()) {
            Random random = new Random();
            if (random.nextFloat() < gameConfig.getDeadFoodProb()){
                foods.add(snakeCoordinate);
            } else {
                field.setCells(snakeCoordinate, GameCell.getNoneCell());
            }
        }
    }

    private void generateFood(int count, Field field) {
        int width = gameConfig.getMaxCoordinates().getX();
        int height = gameConfig.getMaxCoordinates().getY();
        while (this.foods.size() < count) {
            Coordinates newFoods = Coordinates.getRandomCoordinates(width, height);
            if (GameCell.isEmpty(field.in(newFoods))) {
                newFoods = Coordinates.getRandomCoordinates(width, height);
                this.foods.add(newFoods);
                drawFood();
            }

        }
        drawFood();
    }

    public Field makeRepresentation() {
        field = new Field(gameConfig.getMaxCoordinates(), GameCell.getNoneCell());
        drawFood();
        gamers.stream()
                .filter(Predicate.not(Gamer::isDead))
                .forEach(this::drawSnake);
        return field;
    }

    private void drawFood() {
        field.setCells(foods, GameCell.getFoodCell());
    }

    private void drawSnake(Gamer g) {
        field.setCells(g.getSnakeCoordinates(), GameCell.getGamerCell(g.getId(), g.getColor()));
    }

    private void drawSnakePoint(Coordinates coordinates, Gamer gamer) {
        field.setCells(coordinates, GameCell.getGamerCell(gamer.getId(), gamer.getColor()));
    }

    public List<Gamer> getDying() {
        return gamers.stream()
                .filter(Gamer::isDying)
                .collect(Collectors.toList());
    }

    public Queue<Gamer> getDead() {
        return gamers.stream()
                .filter(Gamer::isDead)
                .filter(gamer -> gamer.getSnake().isDying())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    synchronized public void removeDead() {
        gamers = gamers.stream()
                .filter(gamer ->
                        !gamer.getSnake().getKeyPoints().isEmpty() && !gamer.getSnake().isDying())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public int getAndAddStateId() {
        return gameStateId.getAndAdd(1);
    }

    public int getGameStateId() {
        return gameStateId.get();
    }

    public Iterable<? extends Gamer> getSorted() {
        return gamers.stream()
                .sorted(Comparator.comparingInt(Gamer::getScore))
                .collect(Collectors.toList());
    }

    synchronized public void makeZombiesFromInactiveGamers() {
        for (Gamer gamer : gamers) {
            if (activeGamersPerCycle.contains(Pair.of(gamer.getIpAddress(), gamer.getPort()))) {
                gamer.getSnake().becomeZombie();
            }
        }
    }

    public Optional<Gamer> getPotentialDeputy() {
       return gamers.stream().filter(gamer -> !gamer.isMaster() && !gamer.isViewer()).findFirst();
    }
}
