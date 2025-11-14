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
        boolean sensorTest = random.nextDouble() > 0.1;
        boolean memoryTest = random.nextDouble() > 0.05;
        boolean motorTest = random.nextDouble() > 0.15;
        boolean result = sensorTest && memoryTest && motorTest;

        if (csvLogger != null) {
            csvLogger.logEvent(this, result ? EventType.SELF_TEST_SUCCESS : EventType.SELF_TEST_FAILED);
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
        failedAttempts = 0;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CALIBRATION_COMPLETE, "Сброс числа неудач");
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.15;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONNECTIVITY_CHECK, connected ? "OK" : "ОШИБКА");
        }
        return connected;
    }

    public boolean authenticateUser(String fingerprintId) {
        if (authorizedUsers.containsKey(fingerprintId)) {
            failedAttempts = 0;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.AUTH_SUCCESS, authorizedUsers.get(fingerprintId));
            }
            return true;
        } else {
            failedAttempts++;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.AUTH_FAILED, "Неудачных попыток " + failedAttempts);
            }
            if (failedAttempts >= 3) {
                sendAlarm("Многократные неудачные попытки доступа");
            }
            return false;
        }
    }

    public void addUser(String fingerprintId, String userName) {
        authorizedUsers.put(fingerprintId, userName);
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.USER_ADDED, userName);
        }
    }

    public void removeUser(String fingerprintId) {
        String userName = authorizedUsers.remove(fingerprintId);
        if (userName != null && csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED, "Пользователь исключен: " + userName);
        }
    }

    public void lockDoor() {
        lockStatus = "Заблокирован";
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.DOOR_LOCKED);
        }
    }

    public void unlockDoor() {
        lockStatus = "Открыт";
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.DOOR_UNLOCKED);
        }
    }

    public void toggleFingerprintScanner() {
        fingerprintEnabled = !fingerprintEnabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED, "Сканер отпечатков: " + fingerprintEnabled);
        }
    }

    public void setAutoLockDelay(int seconds) {
        autoLockDelay = seconds;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED, "Автоблокировка: " + seconds);
        }
    }

    public int getFailedAttempts() { return failedAttempts; }
    public boolean isFingerprintEnabled() { return fingerprintEnabled; }
    public boolean isFaceRecognitionEnabled() { return faceRecognitionEnabled; }
    public String getLockStatus() { return lockStatus; }
    public int getAutoLockDelay() { return autoLockDelay; }
    public Map<String, String> getAuthorizedUsers() { return new HashMap<>(authorizedUsers); }

    public void setFailedAttempts(int attempts) {
        this.failedAttempts = Math.max(0, attempts);
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED, "Неудачных попыток: " + attempts);
        }
    }

    public void setFingerprintEnabled(boolean enabled) {
        this.fingerprintEnabled = enabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED, "Сканер отпечатков: " + enabled);
        }
    }

    public void setFaceRecognitionEnabled(boolean enabled) {
        this.faceRecognitionEnabled = enabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED, "Распознавание лиц: " + enabled);
        }
    }

    public void setLockStatus(String status) {
        if (status != null && !status.isBlank()) {
            this.lockStatus = status;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED, "Статус замка: " + status);
            }
        }
    }
}
