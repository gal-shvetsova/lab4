package fit.networks.game;

import fit.networks.game.gamefield.Cell;
import fit.networks.game.gamefield.Field;
import fit.networks.game.gamefield.GameCell;
import fit.networks.gamer.Gamer;
import fit.networks.protocol.Protocol;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.net.InetAddress;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Game {
    private final static Logger logger = Logger.getLogger("Game");
    private GameConfig gameConfig;
    private Queue<Gamer> gamers = new ConcurrentLinkedQueue<>();
    private Queue<Pair<InetAddress, Integer>> activeGamersPerCycle = new ConcurrentLinkedQueue<>();
    private Deque<Coordinates> foods = new ArrayDeque<>();
    private AtomicInteger gameStateId = new AtomicInteger(0);

    public Optional<Gamer> getGamerByAddress(InetAddress inetAddress, int port) {
        return gamers.stream().filter(x -> x.getIpAddress().equals(inetAddress) && x.getPort() == port).findFirst();
    }

    public Gamer getGamerById(int id) {
        return gamers.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public boolean deputyAbsent() {
        return !gamers.stream().allMatch(Gamer::isDeputy);
    }

    public Optional<Gamer> getDeputy() {
        return gamers.stream().filter(Gamer::isDeputy).findFirst();
    }

    public Game(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Game(GameConfig gameConfig, Deque<Coordinates> foods) {
        this.gameConfig = gameConfig;
        this.foods = foods;
    }

    public void addAliveGamer(InetAddress inetAddress, int port) {
        this.activeGamersPerCycle.add(Pair.of(inetAddress, port));
    }

    synchronized public Queue<Gamer> getAliveGamers() {
        return gamers;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public void addGamer(Gamer gamer) {
        gamers.add(gamer);
    }

    public boolean hasAliveGamers() {
        return !gamers.stream().allMatch(gamer -> gamer.isViewer() || gamer.isDead());
    }

    public Deque<Coordinates> getFoodCoordinates() {
        return foods;
    }

    synchronized public Field makeMasterRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates(), GameCell.getNoneCell());
        int neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        generateFood(neededFoods, field);
        drawFood(field);
        logger.info(foods.toString() + " foos ");
        for (Gamer g : gamers) {
            logger.info(gamers.size() + " ");
            for (Coordinates c : g.getSnakeCoordinates()) {
                logger.info("cell " + g.getSnakeCoordinates().toString());
                if (GameCell.isFood(field.in(c)) && g.getSnake().isHead(c)) {
                    g.getSnake().grow();
                    g.addScore();
                    drawSnakePoint(field, c, g);
                    foods.remove(c);
                } else if (GameCell.isEmpty(field.in(c))) {
                    drawSnakePoint(field, c, g);
                    //   logger.info("none");
                } else {
                    Gamer anotherGamer = getGamerById(GameCell.getId(field.in(c)));
                    //   logger.info("another snake " + anotherGamer.getId());
                    if (anotherGamer.isHead(c)) {
                        anotherGamer.becomeDying();
                    } else {
                        anotherGamer.addScore();
                    }
                    //   logger.info("current snake " + anotherGamer.getId());
                    if (g.isHead(c)) {
                        g.becomeDying();
                    } else {
                        g.addScore();
                    }
                }
            }
        }

        getDying().forEach(g -> {
            field.setCells(g.getSnakeCoordinates(), GameCell.getNoneCell());
        });

        neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        generateFood(neededFoods, field);

        return field;
    }

    private void generateFood(int count, Field field){
        int width = gameConfig.getMaxCoordinates().getX();
        int height = gameConfig.getMaxCoordinates().getY();
        while (this.foods.size() < count) {
            Coordinates newFoods = Coordinates.getRandomCoordinates(width, height);
            while (GameCell.isEmpty(field.in(newFoods))) {
                newFoods = Coordinates.getRandomCoordinates(width, height);
                this.foods.add(newFoods);
                drawFood(field);
            }

        }
    }

    public Field makeRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates(), GameCell.getNoneCell());
        drawFood(field);
        gamers.stream()
                .filter(Predicate.not(Gamer::isDead))
                .forEach(gamer -> drawSnake(field, gamer));
        return field;
    }

    private void drawFood(Field field) {
        field.setCells(foods, GameCell.getFoodCell());
    }

    private void drawSnake(Field field, Gamer g) {
        field.setCells(g.getSnakeCoordinates(), GameCell.getGamerCell(g.getId(), g.getColor()));
    }

    private void drawSnakePoint(Field field, Coordinates coordinates, Gamer gamer) {
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
                        !gamer.getSnake().getKeyPoints().isEmpty() && !gamer.getSnake().isDying() && !gamer.isViewer())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public Optional<Gamer> getMaster() {
        return gamers.stream()
                .filter(Gamer::isMaster)
                .findFirst();
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
