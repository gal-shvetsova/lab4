package fit.networks;

import fit.networks.controller.MessageBuilder;
import fit.networks.controller.SnakeController;
import fit.networks.controller.SnakeControllerImpl;


import java.net.InetAddress;

public class Main {
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("You should write name, " +
                    " the host IP address and port");
            return;
        }
        try {
            String name = args[0];
            InetAddress ipAddress = InetAddress.getByName(args[1]);
            int port = Integer.parseInt(args[2]);
            SnakeControllerImpl.getController(name, ipAddress, port).start();

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
