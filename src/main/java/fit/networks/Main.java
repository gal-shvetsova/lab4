package fit.networks;

import fit.networks.controller.*;
import fit.networks.gui.SnakeGUI;
import fit.networks.view.View;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    static final Logger logger = Logger.getLogger("Main");
    private static final View snakeGui = SnakeGUI.getInstance();

    private static GameController controller;
    private static MessageController messageController = MessageControllerImpl.getInstance();
    private static MessageHandler messageHandler = MessageHandlerImpl.getInstance();
    private static ProtoMessagesListener protoMessagesListener = ProtoMessagesListenerImpl.getListener();

    public static void main(String[] args) {


        if (args.length < 3) {
            System.out.println("You should write name, " +
                    " the host IP address and port");
            return;
        }
            String name = args[0];
        InetAddress ipAddress = null;
        try {
            ipAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            logger.log(Level.OFF, "pizdec", e);
            System.exit(Level.OFF.intValue());
        }
        int port = Integer.parseInt(args[2]);

        controller = GameControllerImpl.getController(name, ipAddress, port, snakeGui);

        controller.start();

    }



}
