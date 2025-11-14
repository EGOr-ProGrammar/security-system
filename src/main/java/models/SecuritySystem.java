package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class SecuritySystem {
    protected String systemId;
    protected String location;
    protected String securityMode;
    protected boolean isArmed;
    protected List<String> eventLog;
    protected int batteryLevel;
    protected int signalStrength;
    protected Random random;
    protected CSVLogger csvLogger; // Прямая ссылка на CSVLogger

    public SecuritySystem(String systemId, String location) {
        this.systemId = systemId;
        this.location = location;
        this.securityMode = "Отключено";
        this.isArmed = false;
        this.eventLog = new ArrayList<>();
        this.random = new Random();
        this.batteryLevel = 80 + random.nextInt(21);
        this.signalStrength = 1 + random.nextInt(5);
    }

    // Установка логгера (внедрение зависимости)
    public void setCsvLogger(CSVLogger csvLogger) {
        this.csvLogger = csvLogger;
    }

    public abstract boolean performSelfTest();
    public abstract String simulateEmergency();
    public abstract String getStatusReport();
    public abstract void calibrateSensors();
    public abstract boolean checkConnectivity();

    public void setSecurityMode(String mode) {
        if (mode.equals("Отключено") || mode.equals("Дома") || mode.equals("Отсутствие")) {
            this.securityMode = mode;
            logEvent("Режим безопасности изменен на: " + mode);
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.MODE_CHANGED, mode);
            }
        } else {
            throw new IllegalArgumentException("Недопустимый режим безопасности");
        }
    }

    public void armSystem() {
        if (!isArmed) {
            isArmed = true;
            logEvent("Система переведена в режим охраны");
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.SYSTEM_ARMED);
            }
        }
    }

    public void disarmSystem() {
        if (isArmed) {
            isArmed = false;
            logEvent("Система снята с охраны");
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.SYSTEM_DISARMED);
            }
        }
    }

    public String sendAlarm(String alarmType) {
        String alarmMessage = "ТРЕВОГА: " + alarmType + " - " + java.time.LocalDateTime.now();
        logEvent(alarmMessage);
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.EMERGENCY_SIMULATED, alarmType);
        }
        return alarmMessage;
    }

    protected void logEvent(String event) {
        String timestamp = java.time.LocalDateTime.now().toString();
        eventLog.add("[" + timestamp + "] " + event);
        if (eventLog.size() > 50) {
            eventLog.removeFirst();
        }
    }

    public void updateSensorStatus() {
        batteryLevel = Math.max(0, Math.min(100, batteryLevel + random.nextInt(8) - 5));
        signalStrength = Math.max(1, Math.min(5, signalStrength + random.nextInt(3) - 1));
    }

    public String getSystemId() { return systemId; }
    public String getLocation() { return location; }
    public String getSecurityMode() { return securityMode; }
    public boolean isArmed() { return isArmed; }
    public int getBatteryLevel() { return batteryLevel; }
    public int getSignalStrength() { return signalStrength; }
    public List<String> getEventLog() { return new ArrayList<>(eventLog); }

    public void setLocation(String newLocation) {
        this.location = (newLocation != null) && (!newLocation.isBlank()) ? newLocation : this.location;
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Location: " + newLocation);
        }
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = Math.max(0, Math.min(100, batteryLevel));
    }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = Math.max(1, Math.min(5, signalStrength));
    }

    @Override
    public String toString() {
        return String.format("ID: %s, Местоположение: %s, Режим: %s",
                systemId, location, securityMode);
    }
}
