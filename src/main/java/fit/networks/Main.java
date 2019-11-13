package fit.networks;

import fit.networks.controller.SnakeSwingController;
import fit.networks.gui.SnakeGUI;


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

            SnakeSwingController snakeSwingController = new SnakeSwingController(name, ipAddress, port);
            snakeSwingController.start();

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
