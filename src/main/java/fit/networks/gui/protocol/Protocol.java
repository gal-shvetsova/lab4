package fit.networks.gui.protocol;

public class Protocol {
    private static String newGameButtonName = "Новая игра";
    private static String backGameButtonName = "Назад";
    private static String chooseGameButtonName = "Выбрать игру";
    private static Object[] ratingColumnsHeaders = new String[] {"№", "Имя",
            "Очки"};


    public static String getNewGameButtonName() {
        return newGameButtonName;
    }

    public static String getBackButtonName() {
        return backGameButtonName;
    }

    public static String getChooseGameButtonName() {
        return chooseGameButtonName;
    }


    public static Object[] getRatingColumnsHeaders() {
        return ratingColumnsHeaders;
    }
}