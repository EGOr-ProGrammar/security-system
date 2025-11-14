package models;

import models.dto.EmergencyEvent;
import models.dto.SystemStatusReport;

import java.util.Random;

public abstract class SecuritySystem {

    protected String systemId;
    protected String location;
    protected String securityMode;
    protected boolean isArmed;
    protected int batteryLevel;
    protected int signalStrength;
    protected Random random;
    protected CSVLogger csvLogger;

    public SecuritySystem(String systemId, String location) {
        this.systemId = systemId;
        this.location = location;
        this.securityMode = "Отключено";
        this.isArmed = false;
        this.random = new Random();
        this.batteryLevel = 80 + random.nextInt(21);
        this.signalStrength = 1 + random.nextInt(5);
    }

    public void setCsvLogger(CSVLogger csvLogger) {
        this.csvLogger = csvLogger;
    }

    public abstract boolean performSelfTest();

    public abstract EmergencyEvent simulateEmergency();

    public abstract SystemStatusReport getStatusReport();

    public abstract void calibrateSensors();

    public abstract boolean checkConnectivity();

    public void setSecurityMode(String mode) {
        if (mode.equals("Отключено") || mode.equals("Дома") || mode.equals("Отсутствие")) {
            this.securityMode = mode;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.MODE_CHANGED, mode);
            }
        } else {
            throw new IllegalArgumentException("Недопустимый режим безопасности");
        }
    }

    public void armSystem() {
        if (!isArmed) {
            isArmed = true;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.SYSTEM_ARMED);
            }
        }
    }

    public void disarmSystem() {
        if (isArmed) {
            isArmed = false;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.SYSTEM_DISARMED);
            }
        }
    }

    public EmergencyEvent sendAlarm(String alarmType) {
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.EMERGENCY_SIMULATED, alarmType);
        }
        return new EmergencyEvent(
                systemId,
                this.getClass().getSimpleName(),
                alarmType,
                "Тревога: " + alarmType,
                true
        );
    }

    public void updateSensorStatus() {
        int batteryChange = random.nextInt(8) - 5;
        batteryLevel = batteryLevel + batteryChange;

        if (batteryLevel > 100) {
            batteryLevel = 100;
        }
        if (batteryLevel < 0) {
            batteryLevel = 0;
        }

        int signalChange = random.nextInt(3) - 1;
        signalStrength = signalStrength + signalChange;

        if (signalStrength > 5) {
            signalStrength = 5;
        }
        if (signalStrength < 1) {
            signalStrength = 1;
        }
    }

    // Геттеры
    public String getSystemId() {
        return systemId;
    }

    public String getLocation() {
        return location;
    }

    public String getSecurityMode() {
        return securityMode;
    }

    public boolean isArmed() {
        return isArmed;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    // Сеттеры
    public void setLocation(String newLocation) {
        this.location = (newLocation != null) && (!newLocation.isBlank()) ? newLocation : this.location;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED, "Location: " + newLocation);
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
