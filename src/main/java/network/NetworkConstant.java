package network;

/**
 * Константы протокола взаимодействия клиент-сервер
 */
public class NetworkConstant {
    // Команды для работы с системами
    public static final String GET_ALL_SYSTEMS = "GET_ALL_SYSTEMS";
    public static final String GET_SYSTEM = "GET_SYSTEM";
    public static final String ADD_SYSTEM = "ADD_SYSTEM";
    public static final String REMOVE_SYSTEM = "REMOVE_SYSTEM";
    public static final String REMOVE_SYSTEM_BY_ID = "REMOVE_SYSTEM_BY_ID";

    // Команды управления системами
    public static final String ARM_SYSTEM = "ARM_SYSTEM";
    public static final String DISARM_SYSTEM = "DISARM_SYSTEM";
    public static final String SET_SECURITY_MODE = "SET_SECURITY_MODE";

    // Команды диагностики
    public static final String PERFORM_SELF_TEST = "PERFORM_SELF_TEST";
    public static final String SIMULATE_EMERGENCY = "SIMULATE_EMERGENCY";
    public static final String GET_STATUS_REPORT = "GET_STATUS_REPORT";
    public static final String CALIBRATE_SENSORS = "CALIBRATE_SENSORS";
    public static final String CHECK_CONNECTIVITY = "CHECK_CONNECTIVITY";

    // Команды работы с файлами
    public static final String LOAD_SYSTEMS_FROM_FILE = "LOAD_SYSTEMS_FROM_FILE";
    public static final String SET_FILE_NAME = "SET_FILE_NAME";
    public static final String GET_CURRENT_FILE_NAME = "GET_CURRENT_FILE_NAME";
    public static final String GET_SYSTEM_BY_ID = "GET_SYSTEM_BY_ID";
    public static final String SAVE_SYSTEMS_TO_FILE = "SAVE_SYSTEMS_TO_FILE";

    // Команды логирования
    public static final String LOG_ALL_SYSTEMS_STATE = "LOG_ALL_SYSTEMS_STATE";
    public static final String GET_CSV_LOGS = "GET_CSV_LOGS";
    public static final String GET_RECENT_LOGS = "GET_RECENT_LOGS";
    public static final String SET_CSV_LOG_INTERVAL = "SET_CSV_LOG_INTERVAL";

    // Системные команды
    public static final String PING = "PING";
    public static final String GET_SYSTEM_COUNT = "GET_SYSTEM_COUNT";

    // Ключи параметров
    public static final String PARAM_INDEX = "index";
    public static final String PARAM_SYSTEM_ID = "systemId";
    public static final String PARAM_SYSTEM_JSON = "systemJson";
    public static final String PARAM_SYSTEM_TYPE = "systemType";
    public static final String PARAM_MODE = "mode";
    public static final String PARAM_FILE_NAME = "fileName";
    public static final String PARAM_APPEND = "append";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_INTERVAL = "interval";
    public static final String PARAM_LOCATION = "location";
}
