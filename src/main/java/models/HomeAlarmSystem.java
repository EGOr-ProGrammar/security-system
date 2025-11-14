package models;

import models.dto.EmergencyEvent;
import models.dto.HomeAlarmStatusReport;
import models.dto.SystemStatusReport;

public class HomeAlarmSystem extends SecuritySystem {

    private boolean doorSensorsActive;
    private boolean windowSensorsActive;
    private boolean motionSensorsActive;
    private int sensitivityLevel;
    private boolean silentMode;
    private String alarmSound;

    public HomeAlarmSystem(String systemId, String location) {
        super(systemId, location);
        this.doorSensorsActive = true;
        this.windowSensorsActive = true;
        this.motionSensorsActive = true;
        this.sensitivityLevel = 3;
        this.silentMode = false;
        this.alarmSound = "Сирена";
    }

    @Override
    public boolean performSelfTest() {
        boolean doorTest = random.nextDouble() > 0.1;
        boolean windowTest = random.nextDouble() > 0.1;
        boolean motionTest = random.nextDouble() > 0.1;
        boolean result = doorTest && windowTest && motionTest;

        if (csvLogger != null) {
            csvLogger.logEvent(this, result ? EventType.SELF_TEST_SUCCESS : EventType.SELF_TEST_FAILED);
        }

        return result;
    }

    @Override
    public EmergencyEvent simulateEmergency() {
        String[] emergencyTypes = {"Вторжение через дверь", "Разбито окно", "Обнаружено движение"};
        String selectedEmergency = emergencyTypes[random.nextInt(emergencyTypes.length)];

        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.EMERGENCY_SIMULATED);
        }

        return new EmergencyEvent(
                systemId,
                "HomeAlarmSystem",
                selectedEmergency,
                "Экстренная ситуация в домашней сигнализации: " + selectedEmergency,
                true
        );
    }

    @Override
    public SystemStatusReport getStatusReport() {
        return new HomeAlarmStatusReport(
                systemId,
                location,
                securityMode,
                isArmed,
                batteryLevel,
                signalStrength,
                doorSensorsActive,
                windowSensorsActive,
                motionSensorsActive,
                sensitivityLevel,
                silentMode,
                alarmSound
        );
    }

    @Override
    public void calibrateSensors() {
        this.sensitivityLevel = 3;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CALIBRATION_COMPLETE);
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.2;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONNECTIVITY_CHECK);
        }
        return connected;
    }

    public void toggleDoorSensors() {
        this.doorSensorsActive = !this.doorSensorsActive;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void toggleWindowSensors() {
        this.windowSensorsActive = !this.windowSensorsActive;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setSensitivity(int level) {
        if (level >= 1 && level <= 5) {
            this.sensitivityLevel = level;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
            }
        }
    }

    public void toggleSilentMode() {
        this.silentMode = !this.silentMode;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    public void simulateIntrusion() {
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.INTRUSION_DETECTED);
        }
    }

    // Геттеры
    public boolean isDoorSensorsActive() {
        return doorSensorsActive;
    }

    public boolean isWindowSensorsActive() {
        return windowSensorsActive;
    }

    public boolean isMotionSensorsActive() {
        return motionSensorsActive;
    }

    public int getSensitivityLevel() {
        return sensitivityLevel;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public String getAlarmSound() {
        return alarmSound;
    }

    // Сеттеры
    public void setDoorSensorsActive(boolean active) {
        this.doorSensorsActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setWindowSensorsActive(boolean active) {
        this.windowSensorsActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setMotionSensorsActive(boolean active) {
        this.motionSensorsActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setSensitivityLevel(int level) {
        if (level >= 1 && level <= 5) {
            this.sensitivityLevel = level;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
            }
        }
    }

    public void setSilentMode(boolean silent) {
        this.silentMode = silent;
        this.alarmSound = silent ? "Тихий" : "Сирена";
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    public void setAlarmSound(String sound) {
        if (sound != null && !sound.isBlank()) {
            this.alarmSound = sound;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
            }
        }
    }
}
