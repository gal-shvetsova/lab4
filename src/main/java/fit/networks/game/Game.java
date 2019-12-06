package fit.networks.game;

import fit.networks.controller.Message;
import fit.networks.controller.MessageControllerImpl;
import fit.networks.controller.MessageCreator;
import fit.networks.game.gamefield.Cell;
import fit.networks.game.gamefield.Field;
import fit.networks.gamer.Gamer;
import fit.networks.gamer.Role;
import fit.networks.protocol.Protocol;
import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Game {

    private GameConfig gameConfig;
    private Queue<Gamer> gamers = new ConcurrentLinkedQueue<>();
    //private  activeGamersPerCycle = new ArrayList<>();
    private Deque<Coordinates> foods = new ArrayDeque<>();
    private final Logger logger = Logger.getLogger("Game");

    public Optional<Gamer> getGamerByAddress(InetAddress inetAddress, int port) {
        return gamers.stream().filter(x -> x.getIpAddress().equals(inetAddress) && x.getPort() == port).findFirst();
    }

    public Gamer getGamerById(int id) {
        return gamers.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public boolean hasDeputy() {
        return gamers.stream().allMatch(Gamer::isDeputy);
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

    public void setGamers(Queue<Gamer> gamers) {
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
        Field field = new Field(gameConfig.getMaxCoordinates(), new Cell(Protocol.getNoneValue(), Protocol.getNoneColor()));
        field.setCells(foods, new Cell(Protocol.getFoodValue(), Protocol.getFoodColor()));
        removeZombies();
        for (Gamer g : gamers) {
            for (Coordinates c : g.getSnakeCoordinates()) {
                if (field.getValue(c) == Protocol.getFoodValue() && g.getSnake().isHead(c)) {
                    g.getSnake().grow();
                    field.setCells(c, new Cell(g.getId(), g.getColor()));
                    foods.remove(c);
                } else if (field.getValue(c) != Protocol.getFoodValue() && field.getValue(c) != Protocol.getNoneValue()) {
                    Gamer anotherGamer = getGamerById(field.getValue(c));
                    if (anotherGamer.isHead(c)) {
                        anotherGamer.becomeDying();
                        requestNewMaster(anotherGamer);
                    } else {
                        anotherGamer.addPoints();
                    }
                    if (g.isHead(c)) {
                        g.becomeDying();
                        requestNewMaster(g);
                    } else {
                        g.addPoints();
                    }
                } else {
                    field.setCells(c, new Cell(g.getId(), g.getColor()));
                }
            }
        }

        gamers.stream().filter(x-> x.getSnake().isDying()).forEach(g -> {
            field.setCells(g.getSnakeCoordinates(), new Cell(Protocol.getNoneValue(), Protocol.getNoneColor()));
        });

//        removeZombies();

        int neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * gamers.size());
        int width = gameConfig.getMaxCoordinates().getX();
        int height = gameConfig.getMaxCoordinates().getY();

        while (this.foods.size() < neededFoods) {
            Coordinates newFoods = Coordinates.getRandomCoordinates(width, height);
            while (field.getValue(newFoods) != Protocol.getNoneValue()) {
                newFoods = Coordinates.getRandomCoordinates(width, height);
            }
            this.foods.add(newFoods);
            field.setCells(newFoods, new Cell(Protocol.getFoodValue(), Protocol.getFoodColor()));
        }

        return field;
    }

    private void requestNewMaster(Gamer g) {
        if (g.isMaster()) {
            SnakesProto.GameMessage protoMessage = MessageCreator.makeRoleChangeMessage(Role.DEPUTY);
            if (getDeputy().isEmpty()) return;
            Gamer deputy = getDeputy().get();
            deputy.setRole(Role.MASTER);
            Message message = new Message(protoMessage, deputy.getIpAddress(), deputy.getPort());
            MessageControllerImpl.getInstance().sendMessage(message);
        }
    }

    private void registerMaster(Gamer gamer){
        gamer.setRole(Role.MASTER);
    }

    public Field makeRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates(), new Cell(Protocol.getNoneValue(), Protocol.getNoneColor()));
        field.setCells(foods, new Cell(Protocol.getFoodValue(), Protocol.getFoodColor()));
        removeZombies();
        for (Gamer g : gamers) {
            for (Coordinates c : g.getSnakeCoordinates()) {
                if (!g.isDead()) {
                    field.setCells(c, new Cell(g.getId(), g.getColor()));
                }
            }
        }
        return field;
    }

    public List<Gamer> getZombies(){
        return gamers.stream()
                .filter(Gamer::isDead)
                .collect(Collectors.toList());
    }

    public void removeZombies() {
        gamers = gamers.stream()
                .filter(gamer -> !gamer.isDead())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public Gamer getMaster() {
        return gamers.stream()
                .filter(Gamer::isMaster)
                .findFirst()
                .orElse(null);
    }
}
