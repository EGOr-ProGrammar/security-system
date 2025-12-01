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

    protected transient Random random;
    protected transient CSVLogger csvLogger;

    public SecuritySystem(String systemId, String location) {
        this.systemId = systemId;
        this.location = location;
        this.securityMode = "Выключен";
        this.isArmed = false;
        this.batteryLevel = 100;
        this.signalStrength = 5;
        this.random = new Random();
    }

    protected void ensureRandomInitialized() {
        if (this.random == null) {
            this.random = new Random();
        }
    }

    public void armSystem() {
        ensureRandomInitialized();
        this.isArmed = true;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SYSTEM_ARMED);
        }
    }

    public void disarmSystem() {
        ensureRandomInitialized();
        this.isArmed = false;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SYSTEM_DISARMED);
        }
    }

    public void setSecurityMode(String mode) {
        ensureRandomInitialized();
        String[] validModes = {"Выключен", "Дома", "Отсутствие"};
        boolean isValid = false;
        for (String validMode : validModes) {
            if (validMode.equals(mode)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new IllegalArgumentException("Недопустимый режим: " + mode);
        }

        this.securityMode = mode;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.MODE_CHANGED);
        }
    }

    public void updateSensorStatus() {
        ensureRandomInitialized();
        if (random.nextDouble() < 0.05) {
            batteryLevel = Math.max(0, batteryLevel - random.nextInt(10));
        }
        if (random.nextDouble() < 0.03) {
            signalStrength = Math.max(1, Math.min(5, signalStrength + (random.nextInt(3) - 1)));
        }
    }

    // Абстрактные методы, которые должны реализовать подклассы
    public abstract boolean performSelfTest();
    public abstract EmergencyEvent simulateEmergency();
    public abstract SystemStatusReport getStatusReport();
    public abstract void calibrateSensors();
    public abstract boolean checkConnectivity();

    // Геттеры и сеттеры
    public String getSystemId() {
        return systemId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public void setBatteryLevel(int level) {
        this.batteryLevel = Math.max(0, Math.min(100, level));
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(int strength) {
        this.signalStrength = Math.max(1, Math.min(5, strength));
    }

    public void setCsvLogger(CSVLogger csvLogger) {
        this.csvLogger = csvLogger;
    }

    @Override
    public String toString() {
        return String.format("%s [ID: %s, Местоположение: %s, Режим: %s, Охрана: %s, Батарея: %d%%, Сигнал: %d/5]",
                getClass().getSimpleName(), systemId, location, securityMode,
                isArmed ? "ВКЛ" : "ВЫКЛ", batteryLevel, signalStrength);
    }
}
