package fit.networks.protocol;

public class Protocol {
    public enum Type {

        PING(0),           // Ничего не меняем, просто говорим что мы живы
        STEER_UP(1),       // Повернуть голову вверх
        STEER_DOWN(2),     // Повернуть голову вниз
        STEER_LEFT(3),     // Повернуть голову влево
        STEER_RIGHT(4),    // Повернуть голову вправо
        ACK(5),            // Подтверждение сообщения с таким же seq
        STATE(6),          // Состояние игры
        JOIN_PLAY(21),     // Присоединиться к игре в режиме активной игры
        JOIN_WATCH(22),    // Присоединиться к игре в режиме наблюдения
        JOIN_FAIL(23),     // Отказ в присоединении к игре (нет места на поле)
        QUIT(31),          // Выйти из игры, но остаёмся в режиме наблюдения, если продолжаем отправлять пинги главному
        I_AM_MASTER(41);

        private int typeNumber;

        Type(int typeNumber) {
            this.typeNumber = typeNumber;
        }

        public int typeNumber() {
            return typeNumber;
        }
    }


}