package fit.networks.game.gamefield;

import fit.networks.game.Coordinates;

import java.util.Optional;
import java.util.Random;

public class Square {
    private Coordinates startingCoordinates;
    private boolean isAvailable;

    public Square(Coordinates startingCoordinates) {
        this.startingCoordinates = startingCoordinates;
        isAvailable = true;
    }

    public boolean contains(Coordinates coordinates) {
        return ((startingCoordinates.getX() <= coordinates.getX())
                && (startingCoordinates.getY() <= coordinates.getY())
                && (coordinates.getX() <= (startingCoordinates.getX() + 5))
                && (coordinates.getY() <= (startingCoordinates.getY() + 5)));
    }

    public void becomeAvailable() {
        isAvailable = true;
    }

    public void becomeUnavailable() {
        isAvailable = false;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Coordinates randomCoordinates() {
        Random random = new Random();
        int x = random.nextInt(5) + startingCoordinates.getX(),
                y = random.nextInt(5) + startingCoordinates.getY();
        return Coordinates.of(x, y);
    }
}
