package fit.networks;

import fit.networks.gamer.Gamer;
import fit.networks.gui.SnakeGUI;
import fit.networks.session.Session;

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
            Session session = new Session(name, ipAddress, port);
            session.start();
            SnakeGUI snakeGUI = new SnakeGUI();
            snakeGUI.setVisible(true);

        } catch (Exception ex){
            ex.printStackTrace();
        }

    }

}
