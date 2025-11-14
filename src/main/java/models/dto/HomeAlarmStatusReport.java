package models.dto;

/**
 * Отчет о состоянии домашней сигнализации.
 */
public class HomeAlarmStatusReport extends SystemStatusReport {
    private final boolean doorSensorsActive;
    private final boolean windowSensorsActive;
    private final boolean motionSensorsActive;
    private final int sensitivityLevel;
    private final boolean silentMode;
    private final String alarmSound;

    public HomeAlarmStatusReport(String systemId, String location, String securityMode,
                                 boolean isArmed, int batteryLevel, int signalStrength,
                                 boolean doorSensorsActive, boolean windowSensorsActive,
                                 boolean motionSensorsActive, int sensitivityLevel,
                                 boolean silentMode, String alarmSound) {
        super(systemId, location, securityMode, isArmed, batteryLevel, signalStrength);
        this.doorSensorsActive = doorSensorsActive;
        this.windowSensorsActive = windowSensorsActive;
        this.motionSensorsActive = motionSensorsActive;
        this.sensitivityLevel = sensitivityLevel;
        this.silentMode = silentMode;
        this.alarmSound = alarmSound;
    }

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
}
