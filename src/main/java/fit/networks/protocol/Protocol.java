package fit.networks.protocol;

import java.awt.*;

public class Protocol {

    private static final Color FOOD_COLOR = Color.red;
    private static final Color NONE_COLOR = Color.white;
    private static final int FOOD_VALUE = 1;
    private static final int NONE_VALUE = 0;

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
        return 100;
    }

    public static Color getFoodColor() {
        return FOOD_COLOR;
    }

    public static Color getNoneColor() {
        return NONE_COLOR;
    }

    public static int getFoodValue() {
        return FOOD_VALUE;
    }

    public static int getNoneValue() {
        return NONE_VALUE;
    }

    public static String getErrorMessageCantJoin() {
        return "NOT ENOUGH PLACE TO JOIN";
    }
}