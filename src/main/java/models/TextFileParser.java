package models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFileParser {
    private String currentFileName;
    private int totalObjectsFound;
    private int totalPropertiesFound;
    private int totalPropertiesMissing;

    private static final Map<String, Set<String>> VALID_SYSTEM_TYPES = Map.of(
            "homealarmsystem", Set.of("id", "location", "securitymode", "isarmed",
                    "batterylevel", "signalstrength", "doorsensorsactive",
                    "windowsensorsactive", "motionsensorsactive",
                    "sensitivitylevel", "silentmode", "alarmsound"),
            "biometriclock", Set.of("id", "location", "securitymode", "isarmed",
                    "batterylevel", "signalstrength", "failedattempts",
                    "fingerprintenabled", "facerecognitionenabled",
                    "lockstatus", "autolockdelay"),
            "caralarmsystem", Set.of("id", "location", "securitymode", "isarmed",
                    "batterylevel", "signalstrength", "shocksensoractive",
                    "tiltsensoractive", "glassbreaksensoractive",
                    "remotestartenabled", "alarmvolume", "panicmodeduration")
    );

    private static final Map<String, Map<String, String>> PROPERTY_TYPES = Map.of(
            "homealarmsystem", Map.ofEntries(
                    Map.entry("id", "String"),
                    Map.entry("location", "String"),
                    Map.entry("securitymode", "String"),
                    Map.entry("isarmed", "boolean"),
                    Map.entry("batterylevel", "int"),
                    Map.entry("signalstrength", "int"),
                    Map.entry("doorsensorsactive", "boolean"),
                    Map.entry("windowsensorsactive", "boolean"),
                    Map.entry("motionsensorsactive", "boolean"),
                    Map.entry("sensitivitylevel", "int"),
                    Map.entry("silentmode", "boolean"),
                    Map.entry("alarmsound", "String")
            ),
            "biometriclock", Map.ofEntries(
                    Map.entry("id", "String"),
                    Map.entry("location", "String"),
                    Map.entry("securitymode", "String"),
                    Map.entry("isarmed", "boolean"),
                    Map.entry("batterylevel", "int"),
                    Map.entry("signalstrength", "int"),
                    Map.entry("failedattempts", "int"),
                    Map.entry("fingerprintenabled", "boolean"),
                    Map.entry("facerecognitionenabled", "boolean"),
                    Map.entry("lockstatus", "String"),
                    Map.entry("autolockdelay", "int")
            ),
            "caralarmsystem", Map.ofEntries(
                    Map.entry("id", "String"),
                    Map.entry("location", "String"),
                    Map.entry("securitymode", "String"),
                    Map.entry("isarmed", "boolean"),
                    Map.entry("batterylevel", "int"),
                    Map.entry("signalstrength", "int"),
                    Map.entry("shocksensoractive", "boolean"),
                    Map.entry("tiltsensoractive", "boolean"),
                    Map.entry("glassbreaksensoractive", "boolean"),
                    Map.entry("remotestartenabled", "boolean"),
                    Map.entry("alarmvolume", "String"),
                    Map.entry("panicmodeduration", "int")
            )
    );

    public TextFileParser() {
        this.currentFileName = "D:\\docs\\вуз\\3 курс\\java\\lab5\\lab\\src\\main\\java\\security_systems.txt";
        this.totalObjectsFound = 0;
        this.totalPropertiesFound = 0;
        this.totalPropertiesMissing = 0;
    }

    public TextFileParser(String fileName) {
        this.currentFileName = (fileName != null && !fileName.isBlank()) ? fileName
                : "D:\\docs\\вуз\\3 курс\\java\\lab5\\lab\\src\\main\\java\\security_systems.txt";
        this.totalObjectsFound = 0;
        this.totalPropertiesFound = 0;
        this.totalPropertiesMissing = 0;
    }

    /**
     * Основной метод для считывания объектов из файла
     */
    public List<SecuritySystem> readFromFile(String fileName) {
        List<SecuritySystem> systems = new ArrayList<>();
        this.currentFileName = fileName;
        this.totalObjectsFound = 0;
        this.totalPropertiesFound = 0;
        this.totalPropertiesMissing = 0;

        try {
            String fileContent = readFileContent(fileName);
            List<ParsedObject> parsedObjects = parseFileContent(fileContent);

            for (ParsedObject parsedObject : parsedObjects) {
                SecuritySystem system = buildSecuritySystem(parsedObject);
                if (system != null) {
                    systems.add(system);
                    totalObjectsFound++;
                }
            }

        } catch (IOException e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            return new ArrayList<>();
        }

        return systems;
    }

    private String readFileContent(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (!content.isEmpty()) {
                    content.append(" ");
                }
                content.append(line);
            }
        }

        return content.toString();
    }

    /**
     * Парсинг содержимого файла в объекты
     */
    private List<ParsedObject> parseFileContent(String content) {
        List<ParsedObject> objects = new ArrayList<>();

        // Найти все метки объектов
        Pattern objectPattern = Pattern.compile("\\[([a-zA-Z]+)]");
        Matcher matcher = objectPattern.matcher(content);

        String currentObjectType = null;
        int objectStart = -1;

        while (matcher.find()) {
            if (currentObjectType != null) {
                // Finish previous object
                int objectEnd = matcher.start();
                String objectContent = content.substring(objectStart, objectEnd);
                ParsedObject parsedObject = parseObjectContent(currentObjectType, objectContent);
                if (parsedObject != null) {
                    objects.add(parsedObject);
                }
            }

            // Создать новый объект
            currentObjectType = matcher.group(1).toLowerCase();
            objectStart = matcher.end();
        }

        // Обработать последний объект
        if (currentObjectType != null && objectStart != -1) {
            String objectContent = content.substring(objectStart);
            ParsedObject parsedObject = parseObjectContent(currentObjectType, objectContent);
            if (parsedObject != null) {
                objects.add(parsedObject);
            }
        }

        return objects;
    }

    /**
     * Парсинг свойств объекта
     */
    private ParsedObject parseObjectContent(String objectType, String content) {
        String normalizedObjectType = objectType.toLowerCase();

        if (!VALID_SYSTEM_TYPES.containsKey(normalizedObjectType)) {
            System.out.println("Неизвестный тип объекта: " + objectType);
            return null;
        }

        ParsedObject parsedObject = new ParsedObject(normalizedObjectType);
        Set<String> validProperties = VALID_SYSTEM_TYPES.get(normalizedObjectType);

        parseFusedProperties(parsedObject, normalizedObjectType, content, validProperties);

        return parsedObject;
    }

    /**
     * Парсинг свойств объекта без разделителя типа свойство:значение
     */
    private void parseFusedProperties(ParsedObject parsedObject, String objectType, String content, Set<String> validProperties) {
        String normalizedContent = content.toLowerCase();

        for (String property : validProperties) {
            int propertyIndex = normalizedContent.indexOf(property.toLowerCase());
            if (propertyIndex == -1) continue;

            int valueStart = propertyIndex + property.length();
            if (valueStart >= content.length()) continue;

            // Оставить только имя свойства
            while (valueStart < content.length() &&
                    (content.charAt(valueStart) == ':' || Character.isWhitespace(content.charAt(valueStart)))) {
                valueStart++;
            }

            if (valueStart >= content.length()) continue;

            // Значение для свойства
            String valueContent = extractValueUntilNextProperty(content, valueStart, validProperties);

            if (!valueContent.isEmpty()) {
                Object parsedValue = parseValue(objectType, property, valueContent);
                if (parsedValue != null) {
                    parsedObject.properties.put(property, parsedValue);
                    totalPropertiesFound++;
                    System.out.println("Найдено свойство: " + property + " = " + parsedValue);
                } else {
                    totalPropertiesMissing++;
                    System.out.println("Не удалось распарсить свойство: " + property + " = " + valueContent);
                }
            }
        }
    }

    private String extractValueUntilNextProperty(String content, int startPosition, Set<String> validProperties) {
        if (startPosition >= content.length()) {
            return "";
        }

        StringBuilder value = new StringBuilder();
        String remainingContent = content.substring(startPosition).toLowerCase();

        int valueStart = startPosition;
        while (valueStart < content.length() && content.charAt(valueStart) == ':') {
            valueStart++;
        }

        if (valueStart >= content.length()) {
            return "";
        }

        int nextPropertyPos = -1;
        for (String property : validProperties) {
            int pos = remainingContent.indexOf(property);
            if (pos != -1 && (nextPropertyPos == -1 || pos < nextPropertyPos)) {
                nextPropertyPos = pos;
            }
        }

        if (nextPropertyPos != -1) {
            value.append(content, valueStart, startPosition + nextPropertyPos);
        } else {
            value.append(content.substring(valueStart));
        }

        return value.toString().trim();
    }

    private Object parseValue(String objectType, String property, String valueContent) {
        String propertyType = PROPERTY_TYPES.get(objectType).get(property.toLowerCase());

        if (propertyType == null) {
            return null;
        }

        return switch (propertyType) {
            case "String" -> parseStringValue(valueContent);
            case "int" -> parseIntegerValue(valueContent);
            case "double" -> parseDoubleValue(valueContent);
            case "boolean" -> parseBooleanValue(valueContent);
            default -> null;
        };
    }

    private String parseStringValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private Integer parseIntegerValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Find last valid number occurrence
        Pattern numberPattern = Pattern.compile("-?\\d+");
        Matcher matcher = numberPattern.matcher(value);

        Integer lastValidNumber = null;
        while (matcher.find()) {
            try {
                lastValidNumber = Integer.parseInt(matcher.group());
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }

        return lastValidNumber;
    }

    private Double parseDoubleValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Find last valid floating point number
        Pattern doublePattern = Pattern.compile("-?\\d+\\.\\d+");
        Matcher matcher = doublePattern.matcher(value);

        Double lastValidDouble = null;
        while (matcher.find()) {
            try {
                lastValidDouble = Double.parseDouble(matcher.group());
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }

        return lastValidDouble;
    }

    private Boolean parseBooleanValue(String value) {
        if (value == null) {
            return null;
        }

        String lowerValue = value.toLowerCase();

        // Search for true/false occurrences
        if (lowerValue.contains("true")) {
            return true;
        } else if (lowerValue.contains("false")) {
            return false;
        }

        return null;
    }

    private SecuritySystem buildSecuritySystem(ParsedObject parsedObject) {
        return switch (parsedObject.type) {
            case "homealarmsystem" -> buildHomeAlarmSystem(parsedObject);
            case "biometriclock" -> buildBiometricLock(parsedObject);
            case "caralarmsystem" -> buildCarAlarmSystem(parsedObject);
            default -> null;
        };
    }

    private HomeAlarmSystem buildHomeAlarmSystem(ParsedObject parsedObject) {
        String id = (String) parsedObject.properties.getOrDefault("id", "DEFAULT_HOME_" + System.currentTimeMillis());
        String location = (String) parsedObject.properties.getOrDefault("location", "Неизвестное местоположение");

        HomeAlarmSystem system = new HomeAlarmSystem(id, location);

        if (parsedObject.properties.containsKey("batterylevel")) {
            Integer batteryLevel = (Integer) parsedObject.properties.get("batterylevel");
            if (batteryLevel != null) system.setBatteryLevel(batteryLevel);
        }

        if (parsedObject.properties.containsKey("signalstrength")) {
            Integer signalStrength = (Integer) parsedObject.properties.get("signalstrength");
            if (signalStrength != null) system.setSignalStrength(signalStrength);
        }

        if (parsedObject.properties.containsKey("securitymode")) {
            String securityMode = (String) parsedObject.properties.get("securitymode");
            try {
                system.setSecurityMode(securityMode);
            } catch (IllegalArgumentException e) {
                system.setSecurityMode("Отключено");
            }
        }

        if ((Boolean) parsedObject.properties.getOrDefault("isarmed", false)) {
            system.armSystem();
        }

        if (parsedObject.properties.containsKey("doorsensorsactive")) {
            system.setDoorSensorsActive((Boolean) parsedObject.properties.get("doorsensorsactive"));
        }

        if (parsedObject.properties.containsKey("windowsensorsactive")) {
            system.setWindowSensorsActive((Boolean) parsedObject.properties.get("windowsensorsactive"));
        }

        if (parsedObject.properties.containsKey("motionsensorsactive")) {
            system.setMotionSensorsActive((Boolean) parsedObject.properties.get("motionsensorsactive"));
        }

        if (parsedObject.properties.containsKey("sensitivitylevel")) {
            Integer sensitivity = (Integer) parsedObject.properties.get("sensitivitylevel");
            if (sensitivity != null) system.setSensitivityLevel(sensitivity);
        }

        if (parsedObject.properties.containsKey("silentmode")) {
            system.setSilentMode((Boolean) parsedObject.properties.get("silentmode"));
        }

        if (parsedObject.properties.containsKey("alarmsound")) {
            system.setAlarmSound((String) parsedObject.properties.get("alarmsound"));
        }

        return system;
    }

    private BiometricLock buildBiometricLock(ParsedObject parsedObject) {
        String id = (String) parsedObject.properties.getOrDefault("id", "DEFAULT_LOCK_" + System.currentTimeMillis());
        String location = (String) parsedObject.properties.getOrDefault("location", "Неизвестное местоположение");

        BiometricLock lock = new BiometricLock(id, location);

        if (parsedObject.properties.containsKey("batterylevel")) {
            Integer batteryLevel = (Integer) parsedObject.properties.get("batterylevel");
            if (batteryLevel != null) lock.setBatteryLevel(batteryLevel);
        }

        if (parsedObject.properties.containsKey("signalstrength")) {
            Integer signalStrength = (Integer) parsedObject.properties.get("signalstrength");
            if (signalStrength != null) lock.setSignalStrength(signalStrength);
        }

        if (parsedObject.properties.containsKey("securitymode")) {
            String securityMode = (String) parsedObject.properties.get("securitymode");
            try {
                lock.setSecurityMode(securityMode);
            } catch (IllegalArgumentException e) {
                lock.setSecurityMode("Отключено");
            }
        }

        if ((Boolean) parsedObject.properties.getOrDefault("isarmed", false)) {
            lock.armSystem();
        }

        if (parsedObject.properties.containsKey("failedattempts")) {
            Integer failedAttempts = (Integer) parsedObject.properties.get("failedattempts");
            if (failedAttempts != null) lock.setFailedAttempts(failedAttempts);
        }

        if (parsedObject.properties.containsKey("fingerprintenabled")) {
            lock.setFingerprintEnabled((Boolean) parsedObject.properties.get("fingerprintenabled"));
        }

        if (parsedObject.properties.containsKey("facerecognitionenabled")) {
            lock.setFaceRecognitionEnabled((Boolean) parsedObject.properties.get("facerecognitionenabled"));
        }

        if (parsedObject.properties.containsKey("autolockdelay")) {
            Integer autoLockDelay = (Integer) parsedObject.properties.get("autolockdelay");
            if (autoLockDelay != null) lock.setAutoLockDelay(autoLockDelay);
        }

        if (parsedObject.properties.containsKey("lockstatus")) {
            lock.setLockStatus((String) parsedObject.properties.get("lockstatus"));
        }

        return lock;
    }

    private CarAlarmSystem buildCarAlarmSystem(ParsedObject parsedObject) {
        String id = (String) parsedObject.properties.getOrDefault("id", "DEFAULT_CAR_" + System.currentTimeMillis());
        String location = (String) parsedObject.properties.getOrDefault("location", "Неизвестное местоположение");

        CarAlarmSystem system = new CarAlarmSystem(id, location);

        if (parsedObject.properties.containsKey("batterylevel")) {
            Integer batteryLevel = (Integer) parsedObject.properties.get("batterylevel");
            if (batteryLevel != null) system.setBatteryLevel(batteryLevel);
        }

        if (parsedObject.properties.containsKey("signalstrength")) {
            Integer signalStrength = (Integer) parsedObject.properties.get("signalstrength");
            if (signalStrength != null) system.setSignalStrength(signalStrength);
        }

        if (parsedObject.properties.containsKey("securitymode")) {
            String securityMode = (String) parsedObject.properties.get("securitymode");
            try {
                system.setSecurityMode(securityMode);
            } catch (IllegalArgumentException e) {
                system.setSecurityMode("Отключено");
            }
        }

        if ((Boolean) parsedObject.properties.getOrDefault("isarmed", false)) {
            system.armSystem();
        }

        if (parsedObject.properties.containsKey("shocksensoractive")) {
            system.setShockSensorActive((Boolean) parsedObject.properties.get("shocksensoractive"));
        }

        if (parsedObject.properties.containsKey("tiltsensoractive")) {
            system.setTiltSensorActive((Boolean) parsedObject.properties.get("tiltsensoractive"));
        }

        if (parsedObject.properties.containsKey("glassbreaksensoractive")) {
            system.setGlassBreakSensorActive((Boolean) parsedObject.properties.get("glassbreaksensoractive"));
        }

        if (parsedObject.properties.containsKey("remotestartenabled")) {
            system.setRemoteStartEnabled((Boolean) parsedObject.properties.get("remotestartenabled"));
        }

        if (parsedObject.properties.containsKey("panicmodeduration")) {
            Integer panicModeDuration = (Integer) parsedObject.properties.get("panicmodeduration");
            if (panicModeDuration != null) system.setPanicModeDuration(panicModeDuration);
        }

        if (parsedObject.properties.containsKey("alarmvolume")) {
            system.setAlarmVolume((String) parsedObject.properties.get("alarmvolume"));
        }

        return system;
    }

    /**
     * Хранит информацию о спаршенных объектах
     */
    private static class ParsedObject {
        String type;
        Map<String, Object> properties;

        ParsedObject(String type) {
            this.type = type;
            this.properties = new HashMap<>();
        }
    }

    public void printStatistics() {
        System.out.println("\n=== СТАТИСТИКА ЧТЕНИЯ ФАЙЛА ===");
        System.out.println("Файл: " + currentFileName);
        System.out.println("Найдено объектов: " + totalObjectsFound);
        System.out.println("Успешно прочитано свойств: " + totalPropertiesFound);
        System.out.println("Не найдено свойств: " + totalPropertiesMissing);

        if (totalObjectsFound > 0) {
            double successRate = (double) totalPropertiesFound / (totalPropertiesFound + totalPropertiesMissing) * 100;
            System.out.printf("Процент успешного чтения: %.1f%%\n", successRate);
        }
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String fileName) {
        this.currentFileName = fileName;
    }
}