package models;

import java.util.HashMap;
import java.util.Map;

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
            csvLogger.logEvent(systemId, result ? EventType.SELF_TEST_SUCCESS : EventType.SELF_TEST_FAILED);
        }
        return result;
    }

    @Override
    public String simulateEmergency() {
        String[] emergencies = {"Взлом двери", "Разбитие окна", "Обнаружение движения", "Пожар"};
        String emergency = emergencies[random.nextInt(emergencies.length)];
        return sendAlarm(emergency);
    }

    @Override
    public String getStatusReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("Датчики дверей", doorSensorsActive ? "Активны" : "Неактивны");
        report.put("Датчики окон", windowSensorsActive ? "Активны" : "Неактивны");
        report.put("Датчики движения", motionSensorsActive ? "Активны" : "Неактивны");
        report.put("Уровень чувствительности", sensitivityLevel);
        report.put("Тихий режим", silentMode ? "ВКЛ" : "ВЫКЛ");
        report.put("Звук сигнала", alarmSound);
        report.put("Уровень батареи", batteryLevel + "%");
        report.put("Сила сигнала", signalStrength + "/5");
        return "Отчет домашней сигнализации:\n" + report.toString();
    }

    @Override
    public void calibrateSensors() {
        sensitivityLevel = Math.max(1, Math.min(5, sensitivityLevel + random.nextInt(3) - 1));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CALIBRATION_COMPLETE, "Sensitivity: " + sensitivityLevel);
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.2;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONNECTIVITY_CHECK, connected ? "OK" : "FAILED");
        }
        return connected;
    }

    public void toggleDoorSensors() {
        doorSensorsActive = !doorSensorsActive;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Door sensors: " + doorSensorsActive);
        }
    }

    public void toggleWindowSensors() {
        windowSensorsActive = !windowSensorsActive;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Window sensors: " + windowSensorsActive);
        }
    }

    public void setSensitivity(int level) {
        if (level >= 1 && level <= 5) {
            sensitivityLevel = level;
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Sensitivity: " + level);
            }
        }
    }

    public void toggleSilentMode() {
        silentMode = !silentMode;
        alarmSound = silentMode ? "Без звука" : "Сирена";
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Silent mode: " + silentMode);
        }
    }

    public void simulateIntrusion() {
        if (isArmed) {
            sendAlarm("Обнаружено вторжение!");
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.INTRUSION_DETECTED);
            }
        }
    }

    // Геттеры
    public boolean isDoorSensorsActive() { return doorSensorsActive; }
    public boolean isWindowSensorsActive() { return windowSensorsActive; }
    public boolean isMotionSensorsActive() { return motionSensorsActive; }
    public int getSensitivityLevel() { return sensitivityLevel; }
    public boolean isSilentMode() { return silentMode; }
    public String getAlarmSound() { return alarmSound; }

    public void setDoorSensorsActive(boolean active) {
        this.doorSensorsActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Door sensors: " + active);
        }
    }

    public void setWindowSensorsActive(boolean active) {
        this.windowSensorsActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Window sensors: " + active);
        }
    }

    public void setMotionSensorsActive(boolean active) {
        this.motionSensorsActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Motion sensors: " + active);
        }
    }

    public void setSensitivityLevel(int level) {
        if (level >= 1 && level <= 5) {
            this.sensitivityLevel = level;
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Sensitivity: " + level);
            }
        }
    }

    public void setSilentMode(boolean silent) {
        this.silentMode = silent;
        this.alarmSound = silent ? "Без звука" : "Сирена";
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Silent mode: " + silent);
        }
    }

    public void setAlarmSound(String sound) {
        if (sound != null && !sound.isBlank()) {
            this.alarmSound = sound;
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Alarm sound: " + sound);
            }
        }
    }
}
