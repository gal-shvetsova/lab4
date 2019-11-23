package fit.networks.gui.protocol;

public class Protocol {
    private static Object[] ratingColumnsHeaders = new String[] {"№", "Имя",
            "Очки"};
    private static Object[] allGamesColumnsHeaders = new String[] {"Ведущий", "#",
            "Размер", "Еда"};

    public static String getLeaveGameButtonName(){
        return "Покинуть игру";
    }

    public static String getNewGameButtonName() {
        return "Новая игра";
    }

    public static String getBackButtonName() {
        return "Назад";
    }

    public static String getChooseGameButtonName() {
        return "Выбрать игру";
    }


    public static Object[] getRatingColumnsHeaders() {
        return ratingColumnsHeaders;
    }

    public static String getLeadingLabelName() {
        return "Ведущий:";
    }

    public static String getSizeLabelName() {
        return "Размер:";
    }

    public static String getFoodLabelName() {
        return "Еда:";
    }

    public static Object[] getAllGamesColumnsHeaders() {
        return allGamesColumnsHeaders;
    }

    public static String getWidthLabelName() {
        return "Ширина";
    }

    public static String getHeightLabelName() {
        return "Высота";
    }

    public static String getFoodStaticName() {
        return "Количество еды";
    }

    public static String getDelayMsName() {
        return "Задержка";
    }

    public static String getDeadFoodProbName() {
        return "Коэффициент";
    }

    public static String getFoodPerPlayerName() {
        return "Количество еды на игрока";
    }

    public static String getOkButtonName() {
        return "ОК";
    }

    public static String getCancelButtonName() {
        return "Отменить";
    }

    public static String getJoinGameButtonName() {
        return "Присоединиться";
    }
}