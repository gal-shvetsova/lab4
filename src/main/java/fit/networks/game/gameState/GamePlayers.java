package fit.networks.game.gameState;

import fit.networks.gamer.Gamer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class GamePlayers {
    private Gamer master;
    private List<Gamer> otherGamers = new ArrayList<>();

    public GamePlayers(Gamer master, List<Gamer> otherGamers) {
        this.master = master;
        this.otherGamers = otherGamers;
    }

    public GamePlayers(Gamer master){
        this.master = master;
    }

    public Gamer getMaster() {
        return master;
    }

    public void setMaster(Gamer master){
        this.master = master;
    }

    public boolean setMaster() {
        if (otherGamers.isEmpty()) return false;
        UUID min = otherGamers.get(0).getId();
        for (Gamer gamer : otherGamers) {
            if (gamer.getId().compareTo(min) < 0){
                master = gamer;
                return true;
            }
        }
        master = otherGamers.get(0);
        return true;
    }

    public List<Gamer> getOtherGamers() {
        return otherGamers;
    }

    public void setOtherGamers(List<Gamer> otherGamers) {
        this.otherGamers = otherGamers;
    }

    public void addOtherGamer(Gamer gamer){
        otherGamers.add(gamer);
    }

    public void removeOtherGamer(Gamer gamer){
        otherGamers.remove(gamer);
    }
}