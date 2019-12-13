package fit.networks.game;

import fit.networks.game.gamefield.Field;
import fit.networks.game.gamefield.GameCell;
import fit.networks.gamer.Gamer;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
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

    public Game(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Game(GameConfig gameConfig, Deque<Coordinates> foods) {
        this.gameConfig = gameConfig;
        this.foods = foods;
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

        getDying().forEach(g -> field.setCells(g.getSnakeCoordinates(), GameCell.getNoneCell()));

        neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        generateFood(neededFoods, field);
        return field;
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
    }

    public Field makeRepresentation() {
        field = new Field(gameConfig.getMaxCoordinates(), GameCell.getNoneCell());
        drawFood();
        gamers.stream()
                .filter(Predicate.not(Gamer::isDead))
                .forEach(gamer -> drawSnake(gamer));
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
}
