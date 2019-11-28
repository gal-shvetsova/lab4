package fit.networks.protocol;

public class Protocol {

    public static String getMulticastAddressName() {
        return "239.192.0.4";
    }

    public static Integer getMulticastPort() {
        return 9192;
    }

    public static Integer getMessageQueueCapacity() {
        return 1000;
    }

    public static long getMessageReceivingInterval() {
        return 1500;
    }
}