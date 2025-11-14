package models;

public class CarAlarmSystem extends SecuritySystem {

    private boolean shockSensorActive;
    private boolean tiltSensorActive;
    private boolean glassBreakSensorActive;
    private boolean remoteStartEnabled;
    private String alarmVolume;
    private int panicModeDuration;

    public CarAlarmSystem(String systemId, String location) {
        super(systemId, location);
        this.shockSensorActive = true;
        this.tiltSensorActive = true;
        this.glassBreakSensorActive = true;
        this.remoteStartEnabled = false;
        this.alarmVolume = "Средняя";
        this.panicModeDuration = 30;
    }

    @Override
    public boolean performSelfTest() {
        boolean shockTest = random.nextDouble() > 0.1;
        boolean tiltTest = random.nextDouble() > 0.15;
        boolean glassTest = random.nextDouble() > 0.1;
        boolean sirenTest = random.nextDouble() > 0.05;
        boolean result = shockTest && tiltTest && glassTest && sirenTest;

        if (csvLogger != null) {
            csvLogger.logEvent(this, result ? EventType.SELF_TEST_SUCCESS : EventType.SELF_TEST_FAILED);
        }

        return result;
    }

    @Override
    public EmergencyEvent simulateEmergency() {
        String[] emergencyTypes = {"Удар по автомобилю", "Наклон транспорта", "Разбито стекло"};
        String selectedEmergency = emergencyTypes[random.nextInt(emergencyTypes.length)];

        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.EMERGENCY_SIMULATED);
        }

        return new EmergencyEvent(
                systemId,
                "CarAlarmSystem",
                selectedEmergency,
                "Тревога в автомобильной сигнализации: " + selectedEmergency,
                true
        );
    }

    @Override
    public SystemStatusReport getStatusReport() {
        return new CarAlarmStatusReport(
                systemId,
                location,
                securityMode,
                isArmed,
                batteryLevel,
                signalStrength,
                shockSensorActive,
                tiltSensorActive,
                glassBreakSensorActive,
                remoteStartEnabled,
                alarmVolume,
                panicModeDuration
        );
    }

    @Override
    public void calibrateSensors() {
        this.shockSensorActive = true;
        this.tiltSensorActive = true;
        this.glassBreakSensorActive = true;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CALIBRATION_COMPLETE);
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.25;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONNECTIVITY_CHECK);
        }
        return connected;
    }

    public void activatePanicMode() {
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.PANIC_MODE_ACTIVATED);
        }
    }

    public void toggleShockSensor() {
        this.shockSensorActive = !this.shockSensorActive;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void toggleTiltSensor() {
        this.tiltSensorActive = !this.tiltSensorActive;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setAlarmVolume(String volume) {
        if (volume.equals("Низкая") || volume.equals("Средняя") || volume.equals("Высокая")) {
            this.alarmVolume = volume;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
            }
        }
    }

    public void simulateImpact() {
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.IMPACT_DETECTED);
        }
    }

    public void toggleRemoteStart() {
        this.remoteStartEnabled = !this.remoteStartEnabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    // Геттеры
    public boolean isShockSensorActive() {
        return shockSensorActive;
    }

    public boolean isTiltSensorActive() {
        return tiltSensorActive;
    }

    public boolean isGlassBreakSensorActive() {
        return glassBreakSensorActive;
    }

    public boolean isRemoteStartEnabled() {
        return remoteStartEnabled;
    }

    public String getAlarmVolume() {
        return alarmVolume;
    }

    public int getPanicModeDuration() {
        return panicModeDuration;
    }

    // Сеттеры
    public void setShockSensorActive(boolean active) {
        this.shockSensorActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setTiltSensorActive(boolean active) {
        this.tiltSensorActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setGlassBreakSensorActive(boolean active) {
        this.glassBreakSensorActive = active;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.SENSOR_TOGGLED);
        }
    }

    public void setRemoteStartEnabled(boolean enabled) {
        this.remoteStartEnabled = enabled;
        if (csvLogger != null) {
            csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
        }
    }

    public void setPanicModeDuration(int duration) {
        if (duration > 0) {
            this.panicModeDuration = duration;
            if (csvLogger != null) {
                csvLogger.logEvent(this, EventType.CONFIG_CHANGED);
            }
        }
    }
}
