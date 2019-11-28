package fit.networks;

import fit.networks.controller.GameControllerImpl;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Main {
    static final Logger logger = Logger.getLogger("Main");
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
        GameControllerImpl.getController(name, ipAddress, port).start();


    }

}
