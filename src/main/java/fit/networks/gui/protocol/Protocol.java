package fit.networks.gui.protocol;

public class Protocol {
    private static String newGameButtonName = "Новая игра";
    private static String backGameButtonName = "Назад";
    private static String chooseGameButtonName = "Выбрать игру";
    private static String leadingLabelName = "Ведущий:";
    private static String sizeLabelName = "Размер:";
    private static String  foodLabelName = "Еда:";
    private static Object[] ratingColumnsHeaders = new String[] {"№", "Имя",
            "Очки"};
    private static Object[] allGamesColumnsHeaders = new String[] {"Ведущий", "№",
            "Размер", "Еда", "Вход"};


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

    public static String getLeadingLabelName() {
        return leadingLabelName;
    }

    public static String getSizeLabelName() {
        return sizeLabelName;
    }

    public static String getFoodLabelName() {
        return foodLabelName;
    }

    public static Object[] getAllGamesColumnsHeaders() {
        return allGamesColumnsHeaders;
    }
}