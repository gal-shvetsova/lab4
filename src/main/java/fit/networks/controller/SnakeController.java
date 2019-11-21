package fit.networks.controller;

import fit.networks.protocol.SnakesProto;

import java.net.InetAddress;


public abstract class SnakeController {
    private InetAddress inetAddress;
    private int port;



    abstract void  pingProcessing(InetAddress inetAddress, int port);

    InetAddress getInetAddress() {
        return inetAddress;
    }

    int getPort() {
        return port;
    }
    //  abstract void announcementProcessing();

}
