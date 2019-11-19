package fit.networks.protocol;

import fit.networks.game.Coordinates;
import fit.networks.game.snake.Snake;

import java.util.concurrent.atomic.AtomicInteger;

public class ProtoMaker {
    private AtomicInteger messageSeq = new AtomicInteger(0);


    private SnakesProto.GameState.Snake makeSnakeMessage(Snake snake){
        SnakesProto.GameState.Snake.Builder snakeMessage = SnakesProto.GameState.Snake.newBuilder();
        snakeMessage.setPlayerId(snake.getGamer().getId());
        snakeMessage.setState(snake.isAlive() ? SnakesProto.GameState.Snake.SnakeState.ALIVE : SnakesProto.GameState.Snake.SnakeState.ZOMBIE);
        int lastX = -1;
        int lastY = -1;
        for (Coordinates coordinates: snake) {
            if (snakeMessage.getPointsList().isEmpty() || lastX != coordinates.getX() || lastY != coordinates.getY()) {
                SnakesProto.GameState.Coord.Builder coord = SnakesProto.GameState.Coord.newBuilder();
                coord.setX(coordinates.getX() - lastX);
                coord.setY(coordinates.getY() - lastY); //todo: check this
                snakeMessage.addPoints(coord.build());
                lastX = coordinates.getX();
                lastY = coordinates.getY();
            }
        }
        return snakeMessage.build();
    }

    public SnakesProto.GameMessage makePingMessage(){
        SnakesProto.GameMessage.Builder message = SnakesProto.GameMessage.newBuilder();
        SnakesProto.GameMessage.PingMsg.Builder pingMessage = SnakesProto.GameMessage.PingMsg.newBuilder();
        message.setMsgSeq(messageSeq.addAndGet(1));
        message.setPing(pingMessage.build());
        return message.build();
    }


}
