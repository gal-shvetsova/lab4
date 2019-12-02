package fit.networks.game;

import fit.networks.game.gamefield.Field;
import fit.networks.gamer.Gamer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Game {  // создается только у мастера, сделать создание из предыдущей игры

    private GameConfig gameConfig;
    private int id;
    private ArrayList<Gamer> activeGamers = new ArrayList<>();
    //private  activeGamersPerCycle = new ArrayList<>();
    private ArrayList<Coordinates> foods = new ArrayList<>();

    public Gamer getGamerByAddress(InetAddress inetAddress, int port){
       return activeGamers.stream().filter(x -> x.getIpAddress().equals(inetAddress) && x.getPort() == port).findFirst().orElse(null);
    }

    public Gamer getGamerById(int id){
        return activeGamers.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Game(GameConfig gameConfig) {
        this.gameConfig = gameConfig;

    }

    public Game(GameConfig gameConfig, ArrayList<Coordinates> foods) {
        this.gameConfig = gameConfig;
        this.foods = foods;
    }

    public void setActiveGamers(ArrayList<Gamer> activeGamers) {
        this.activeGamers = activeGamers;
    }

    public void addAliveGamer(InetAddress inetAddress, int port) {
        this.activeGamers.add(new Gamer(inetAddress, port));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Game)) return false;
        return ((Game) obj).id == id;
    }

    public ArrayList<Gamer> getActiveGamers() {
        return activeGamers;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    synchronized public void addGamer(Gamer gamer) {
        activeGamers.add(gamer);
    }

    public boolean hasAliveGamers() {
        return activeGamers.stream().noneMatch(Gamer::isZombie);
    }

    public Field makeRepresentation() {
        Field field = new Field(gameConfig.getMaxCoordinates());
        int neededFoods = gameConfig.getFoodStatic() + (int) (gameConfig.getFoodPerPlayer() * activeGamers.size());
        field.addFoods(foods);

        for (Gamer gamer : activeGamers) {
            switch (field.addGamerSnake(gamer)) {
                case DIE: {
                    foods.addAll(field.getFoodsAfterDie(gamer, gameConfig.getDeadFoodProb()));
                    gamer.becomeZombie();
                    break;
                }
                case GROW: {
                    foods.remove(gamer.getSnakeHeadCoordinates());
                    gamer.getSnake().grow();
                    break;
                }
            }
        }

        foods.addAll(field.generateFoods(neededFoods - foods.size()));
        return field;
    }


    public List<Coordinates> getFoodCoordinates() {
        return foods;
    }

    public Field addGamerSnake() {
        Field field = new Field(gameConfig.getMaxCoordinates());
        for (Gamer g : activeGamers) {
            if (g.isZombie()) continue;
            for (Coordinates c : g.getSnakeCoordinates()) {
                if (field.in(c.getX(), c.getY()).isFood()) {
                    g.getSnake().grow();
                } else if (field.in(c.getX(), c.getY()).isUser()) {
                    Gamer anotherGamer = getGamerById(field.in(c.getX(), c.getY()).getUserId());
                    if (anotherGamer.isHead(c)) {
                        anotherGamer.becomeZombie();
                    }
                    if (g.isHead(c)) {
                        g.becomeZombie();
                    }
                } else {
                    field.setUser(c.getX(), c.getY(), g);
                }
            }
        }

        activeGamers.stream().filter(Gamer::isZombie).forEach(g -> {
            field.removeCoordinates(g.getSnakeCoordinates());
        });
        return field;

    }
}
