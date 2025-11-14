package views;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton класс для централизованного управления конфигурацией приложения.
 * Загружает параметры из файла config.properties и предоставляет методы для доступа к ним.
 */
public class ConfigManager {
    private static volatile ConfigManager instance;
    private final Properties properties;
    private static final String CONFIG_FILE = "config.properties";

    private ConfigManager() {
        properties = new Properties();
        loadProperties();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("Не удалось найти " + CONFIG_FILE + ", используются значения по умолчанию");
                loadDefaults();
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
            loadDefaults();
        }
    }

    private void loadDefaults() {
        properties.setProperty("file.default.path", "security_systems.txt");
        properties.setProperty("file.csv.log", "security_logs.csv");
        properties.setProperty("error.prefix", "Ошибка:");
    }

    public String getString(String key) {
        return properties.getProperty(key, "");
    }

    public String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public String format(String key, Object... args) {
        String template = getString(key);
        return String.format(template, args);
    }

    public boolean hasKey(String key) {
        return properties.containsKey(key);
    }

    public void reload() {
        properties.clear();
        loadProperties();
    }
}
