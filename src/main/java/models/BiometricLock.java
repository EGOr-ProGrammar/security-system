package models;

import java.util.HashMap;
import java.util.Map;

public class BiometricLock extends SecuritySystem {
    private Map<String, String> authorizedUsers;
    private int failedAttempts;
    private boolean fingerprintEnabled;
    private boolean faceRecognitionEnabled;
    private String lockStatus;
    private int autoLockDelay;

    public BiometricLock(String systemId, String location) {
        super(systemId, location);
        this.authorizedUsers = new HashMap<>();
        this.failedAttempts = 0;
        this.fingerprintEnabled = true;
        this.faceRecognitionEnabled = false;
        this.lockStatus = "Открыт";
        this.autoLockDelay = 30;
    }

    @Override
    public boolean performSelfTest() {
        logEvent("Запуск самодиагностики биометрического замка");
        boolean sensorTest = random.nextDouble() > 0.1;
        boolean memoryTest = random.nextDouble() > 0.05;
        boolean motorTest = random.nextDouble() > 0.15;
        boolean result = sensorTest && memoryTest && motorTest;
        logEvent("Самодиагностика замка: " + (result ? "УСПЕШНО" : "ОШИБКА"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, result ? EventType.SELF_TEST_SUCCESS : EventType.SELF_TEST_FAILED);
        }
        return result;
    }

    @Override
    public String simulateEmergency() {
        String[] emergencies = {"Попытка взлома", "Сбой датчика", "Блокировка механизма", "Сбой питания"};
        String emergency = emergencies[random.nextInt(emergencies.length)];
        return sendAlarm(emergency);
    }

    @Override
    public String getStatusReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("Авторизованных пользователей", authorizedUsers.size());
        report.put("Неудачных попыток", failedAttempts);
        report.put("Сканер отпечатков", fingerprintEnabled ? "ВКЛ" : "ВЫКЛ");
        report.put("Распознавание лиц", faceRecognitionEnabled ? "ВКЛ" : "ВЫКЛ");
        report.put("Статус замка", lockStatus);
        report.put("Автоблокировка", autoLockDelay + " сек");
        report.put("Уровень батареи", batteryLevel + "%");
        return "Отчет биометрического замка:\n" + report.toString();
    }

    @Override
    public void calibrateSensors() {
        logEvent("Калибровка биометрических сенсоров");
        failedAttempts = 0;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CALIBRATION_COMPLETE, "Failed attempts reset");
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.15;
        logEvent("Проверка связи с базой данных: " + (connected ? "ОК" : "ОШИБКА"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONNECTIVITY_CHECK, connected ? "OK" : "FAILED");
        }
        return connected;
    }

    public boolean authenticateUser(String fingerprintId) {
        if (authorizedUsers.containsKey(fingerprintId)) {
            logEvent("Доступ разрешен для: " + authorizedUsers.get(fingerprintId));
            failedAttempts = 0;
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.AUTH_SUCCESS, authorizedUsers.get(fingerprintId));
            }
            return true;
        } else {
            failedAttempts++;
            logEvent("Доступ запрещен. Неудачных попыток: " + failedAttempts);
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.AUTH_FAILED, "Attempt " + failedAttempts);
            }
            if (failedAttempts >= 3) {
                sendAlarm("Многократные неудачные попытки доступа");
            }
            return false;
        }
    }

    public void addUser(String fingerprintId, String userName) {
        authorizedUsers.put(fingerprintId, userName);
        logEvent("Добавлен пользователь: " + userName);
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.USER_ADDED, userName);
        }
    }

    public void removeUser(String fingerprintId) {
        String userName = authorizedUsers.remove(fingerprintId);
        if (userName != null) {
            logEvent("Удален пользователь: " + userName);
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "User removed: " + userName);
            }
        }
    }

    public void lockDoor() {
        lockStatus = "Заблокирован";
        logEvent("Дверь заблокирована");
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.DOOR_LOCKED);
        }
    }

    public void unlockDoor() {
        lockStatus = "Открыт";
        logEvent("Дверь открыта");
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.DOOR_UNLOCKED);
        }
    }

    public void toggleFingerprintScanner() {
        fingerprintEnabled = !fingerprintEnabled;
        logEvent("Сканер отпечатков: " + (fingerprintEnabled ? "ВКЛ" : "ВЫКЛ"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Fingerprint: " + fingerprintEnabled);
        }
    }

    public void setAutoLockDelay(int seconds) {
        autoLockDelay = seconds;
        logEvent("Задержка автоблокировки установлена: " + seconds + " сек");
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Auto-lock delay: " + seconds);
        }
    }

    // Геттеры
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isFingerprintEnabled() { return fingerprintEnabled; }
    public boolean isFaceRecognitionEnabled() { return faceRecognitionEnabled; }
    public String getLockStatus() { return lockStatus; }
    public int getAutoLockDelay() { return autoLockDelay; }
    public Map<String, String> getAuthorizedUsers() { return new HashMap<>(authorizedUsers); }

    // Сеттеры для TextFileParser
    public void setFailedAttempts(int attempts) {
        this.failedAttempts = Math.max(0, attempts);
        logEvent("Счётчик неудачных попыток установлен: " + attempts);
    }

    public void setFingerprintEnabled(boolean enabled) {
        this.fingerprintEnabled = enabled;
        logEvent("Сканер отпечатков: " + (enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setFaceRecognitionEnabled(boolean enabled) {
        this.faceRecognitionEnabled = enabled;
        logEvent("Распознавание лиц: " + (enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setLockStatus(String status) {
        if (status != null && !status.isBlank()) {
            this.lockStatus = status;
            logEvent("Статус замка установлен: " + status);
        }
    }
}
