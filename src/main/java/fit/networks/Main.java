package fit.networks;

import fit.networks.controller.*;
import fit.networks.gui.InfoPanel;
import fit.networks.gui.SnakeGUI;
import fit.networks.view.View;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class Main {
    static final Logger logger = Logger.getLogger("Main");

    private static GameController gameController;


    static class Args {
        String name;
        InetAddress inetAddress;
        int port;

        public Args(String name, InetAddress inetAddress, int port) {
            this.name = name;
            this.inetAddress = inetAddress;
            this.port = port;
        }

        public int getPort() {
            return port;
        }

        public InetAddress getInetAddress() {
            return inetAddress;
        }

        public String getName() {
            return name;
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        Args parsedArgs = getArgs(args);
        View snakeGui = SnakeGUI.getInstance();

        GameControllerImpl.init(
                parsedArgs.getName(),
                parsedArgs.getInetAddress(),
                parsedArgs.getPort(),
                snakeGui
        );
        GameControllerImpl.getInstance().start();
    }

    static Args getArgs(String[] args) throws UnknownHostException {
        if (args.length < 3) {
            throw new IllegalArgumentException("You should write name, the host IP address and port");
        }
        return new Args(args[0], InetAddress.getByName(args[1]), Integer.parseInt(args[2]));
    }


}
