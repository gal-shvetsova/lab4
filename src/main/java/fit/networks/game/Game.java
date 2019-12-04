package fit.networks.game;

import fit.networks.game.gamefield.Cell;
import fit.networks.game.gamefield.Field;
import fit.networks.gamer.Gamer;
import fit.networks.protocol.Protocol;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Game {

    private GameConfig gameConfig;
    private List<Gamer> gamers = new ArrayList<>();
    //private  activeGamersPerCycle = new ArrayList<>();
    private Deque<Coordinates> foods = new ArrayDeque<>();

    public Optional<Gamer> getGamerByAddress(InetAddress inetAddress, int port) {
        return gamers.stream().filter(x -> x.getIpAddress().equals(inetAddress) && x.getPort() == port).findFirst();
    }

    public Gamer getGamerById(int id) {
        return gamers.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public boolean hasDeputy() {
        return gamers.stream().allMatch(Gamer::isDeputy);
    }

    public Gamer getDeputy() {
        return gamers.stream().filter(Gamer::isDeputy).findFirst().orElse(null);
    }


    public Game(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }

    public Game(GameConfig gameConfig, Deque<Coordinates> foods) {
        this.gameConfig = gameConfig;
        this.foods = foods;
    }

    public void setGamers(List<Gamer> gamers) {
        this.gamers = gamers;
    }

    public void addAliveGamer(InetAddress inetAddress, int port) {
        this.gamers.add(new Gamer(inetAddress, port));
    }

    public List<Gamer> getAliveGamers() {
        return gamers.stream()
                .filter(Predicate.not(Gamer::isViewer))
                .collect(Collectors.toList());
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    synchronized public void addGamer(Gamer gamer) {
        gamers.add(gamer);
    }

    public boolean hasAliveGamers() {
        return !gamers.stream().allMatch(Gamer::isDead); //todo: add viewers
    }

    public Deque<Coordinates> getFoodCoordinates() {
        return foods;
    }


    public Field makeMasterRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates(), new Cell(Protocol.getNoneValue(), Protocol.getNoneColor()));
        field.setCells(foods, new Cell(Protocol.getFoodValue(), Protocol.getFoodColor()));

        for (Gamer g : gamers) {
            for (Coordinates c : g.getSnakeCoordinates()) {
                if (field.getValue(c) == Protocol.getFoodValue() && g.getSnake().isHead(c)) {
                    g.getSnake().grow();
                    field.setCells(c, new Cell(g.getId(), g.getColor()));
                    foods.remove(c);
                } else if (field.getValue(c) != Protocol.getNoneValue()) {
                    Gamer anotherGamer = getGamerById(field.getValue(c));
                    if (anotherGamer.isHead(c)) {
                        anotherGamer.becomeDying();
                    }
                    if (g.isHead(c)) {
                        g.becomeDying();
                    }
                } else {
                    field.setCells(c, new Cell(g.getId(), g.getColor()));
                }
            }
        }

        gamers.stream().filter(Gamer::isViewer).forEach(g -> {
            field.setCells(g.getSnakeCoordinates(), new Cell(Protocol.getNoneValue(), Protocol.getNoneColor()));
        });

        Deque<Coordinates> foods = new ArrayDeque<>();
        int neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        int width = gameConfig.getMaxCoordinates().getX();
        int height = gameConfig.getMaxCoordinates().getY();

        while (this.foods.size() < neededFoods) {
            Coordinates newFoods = Coordinates.getRandomCoordinates(width, height);
            while (field.getValue(newFoods) != Protocol.getNoneValue()) {
                newFoods = Coordinates.getRandomCoordinates(width, height);
            }
            foods.add(newFoods);
            field.setCells(newFoods, new Cell(Protocol.getFoodValue(), Protocol.getFoodColor()));
        }
        this.foods.addAll(foods);

        return field;
    }

    public Field makeRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates(), new Cell(Protocol.getNoneValue(), Protocol.getNoneColor()));
        field.setCells(foods, new Cell(Protocol.getFoodValue(), Protocol.getFoodColor()));
        for (Gamer g : gamers) {
            for (Coordinates c : g.getSnakeCoordinates()) {
                if (!g.isDead()) {
                    field.setCells(c, new Cell(g.getId(), g.getColor()));
                }
            }
        }
        return field;
    }

    public void removeZombies() {
        gamers = gamers.stream()
                .filter(gamer -> !gamer.isDead())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Gamer getMaster() {
        return gamers.stream()
                .filter(Gamer::isMaster)
                .findFirst()
                .orElse(null);
    }
}
