package fit.networks.protocol;

import java.awt.*;
import java.net.InetAddress;

public class Protocol {

    public static String getMulticastAddressName() {
        return "224.0.0.0";
    }

    public static Integer getMulticastPort() {
        return 5050;
    }

    public static Integer getMessageQueueCapacity() {
        return 1000;
    }

}