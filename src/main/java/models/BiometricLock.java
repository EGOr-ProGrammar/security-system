package models;

import models.dto.BiometricLockStatusReport;
import models.dto.EmergencyEvent;
import models.dto.SystemStatusReport;

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
    public EmergencyEvent simulateEmergency() {
        String[] emergencyTypes = {"Множественные неудачные попытки входа", "Попытка взлома", "Обнаружен несанкционированный доступ"};
        String selectedEmergency = emergencyTypes[random.nextInt(emergencyTypes.length)];

        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.EMERGENCY_SIMULATED);
        }

        return new EmergencyEvent(
                systemId,
                "BiometricLock",
                selectedEmergency,
                "Критическая ситуация в биометрическом замке: " + selectedEmergency,
                true
        );
    }

    @Override
    public SystemStatusReport getStatusReport() {
        return new BiometricLockStatusReport(
                systemId,
                location,
                securityMode,
                isArmed,
                batteryLevel,
                signalStrength,
                authorizedUsers.size(),
                failedAttempts,
                fingerprintEnabled,
                faceRecognitionEnabled,
                lockStatus,
                autoLockDelay
        );
    }

    @Override
    public void calibrateSensors() {
        this.failedAttempts = 0;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CALIBRATION_COMPLETE);
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.15;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONNECTIVITY_CHECK);
        }
        return connected;
    }

    public boolean authenticateUser(String fingerprint) {
        boolean authenticated = authorizedUsers.containsKey(fingerprint);
        if (!authenticated) {
            failedAttempts++;
        } else {
            failedAttempts = 0;
        }
        if (csvLogger != null) {
            csvLogger.logEvent(this, authenticated ? EventType.AUTH_SUCCESS : EventType.AUTH_FAILED);
        }
        return authenticated;
    }

    public void addUser(String fingerprint, String name) {
        authorizedUsers.put(fingerprint, name);
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.USER_ADDED);
        }
    }

    public void removeUser(String fingerprintId) {
        String userName = authorizedUsers.remove(fingerprintId);
        if (userName != null && csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    public void lockDoor() {
        this.lockStatus = "Заблокирован";
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.DOOR_LOCKED);
        }
    }

    public void unlockDoor() {
        this.lockStatus = "Открыт";
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.DOOR_UNLOCKED);
        }
    }

    public void toggleFingerprintScanner() {
        this.fingerprintEnabled = !this.fingerprintEnabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setAutoLockDelay(int seconds) {
        this.autoLockDelay = seconds;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    // Геттеры
    public Map<String, String> getAuthorizedUsers() {
        return new HashMap<>(authorizedUsers);
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isFingerprintEnabled() {
        return fingerprintEnabled;
    }

    public boolean isFaceRecognitionEnabled() {
        return faceRecognitionEnabled;
    }

    public String getLockStatus() {
        return lockStatus;
    }

    public int getAutoLockDelay() {
        return autoLockDelay;
    }

    // Сеттеры
    public void setFailedAttempts(int attempts) {
        this.failedAttempts = Math.max(0, attempts);
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    public void setFingerprintEnabled(boolean enabled) {
        this.fingerprintEnabled = enabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setFaceRecognitionEnabled(boolean enabled) {
        this.faceRecognitionEnabled = enabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setLockStatus(String status) {
        if (status != null && !status.isBlank()) {
            this.lockStatus = status;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
            }
        }
    }
}
