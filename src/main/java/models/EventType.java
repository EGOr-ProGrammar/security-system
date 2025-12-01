package models;

public enum EventType {
    // Системные события
    SYSTEM_ADDED("Система добавлена"),
    SYSTEM_REMOVED("Система удалена"),
    SYSTEM_LOADED("Система загружена"),
    STATE_UPDATE("Обновление состояния"),

    // События охраны
    SYSTEM_ARMED("Система поставлена на охрану"),
    SYSTEM_DISARMED("Система снята с охраны"),

    // Диагностика
    SELF_TEST_SUCCESS("Самодиагностика успешна"),
    SELF_TEST_FAILED("Самодиагностика провалена"),
    CALIBRATION_COMPLETE("Калибровка завершена"),
    CONNECTIVITY_CHECK("Проверка подключения"),

    // Тревоги и экстренные ситуации
    EMERGENCY_SIMULATED("Симуляция аварии"),
    INTRUSION_DETECTED("Обнаружено вторжение"),
    PANIC_MODE_ACTIVATED("Режим паники активирован"),
    IMPACT_DETECTED("Обнаружен удар"),

    // Аутентификация
    AUTH_SUCCESS("Аутентификация успешна"),
    AUTH_FAILED("Аутентификация провалена"),
    USER_ADDED("Пользователь добавлен"),

    // Устройства
    DOOR_LOCKED("Дверь заблокирована"),
    DOOR_UNLOCKED("Дверь разблокирована"),
    SENSOR_TOGGLED("Датчик переключен"),

    // Конфигурация
    CONFIG_CHANGED("Конфигурация изменена"),
    MODE_CHANGED("Режим изменен"),

    // Общие
    INFO("Информация"),
    WARNING("Предупреждение"),
    ERROR("Ошибка"),

    CLIENT_CONNECTED("Клиент подключен"),
    CLIENT_DISCONNECTED("Клиент отключен"),
    COMMAND_RECEIVED("Получена команда"),
    COMMAND_EXECUTED("Команда выполнена"),
    COMMAND_FAILED("Ошибка выполнения команды"),
    FILE_LOADED("Файл загружен"),
    FILE_SAVED("Файл сохранен"),
    SERVER_STARTED("Сервер запущен"),
    SERVER_STOPPED("Сервер остановлен");



    private final String description;

    EventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + " (" + description + ")";
    }
}
