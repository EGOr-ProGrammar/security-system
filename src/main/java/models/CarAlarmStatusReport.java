package models;

/**
 * Отчет о состоянии автомобильной сигнализации.
 */
public class CarAlarmStatusReport extends SystemStatusReport {
    private final boolean shockSensorActive;
    private final boolean tiltSensorActive;
    private final boolean glassBreakSensorActive;
    private final boolean remoteStartEnabled;
    private final String alarmVolume;
    private final int panicModeDuration;

    public CarAlarmStatusReport(String systemId, String location, String securityMode,
                                boolean isArmed, int batteryLevel, int signalStrength,
                                boolean shockSensorActive, boolean tiltSensorActive,
                                boolean glassBreakSensorActive, boolean remoteStartEnabled,
                                String alarmVolume, int panicModeDuration) {
        super(systemId, location, securityMode, isArmed, batteryLevel, signalStrength);
        this.shockSensorActive = shockSensorActive;
        this.tiltSensorActive = tiltSensorActive;
        this.glassBreakSensorActive = glassBreakSensorActive;
        this.remoteStartEnabled = remoteStartEnabled;
        this.alarmVolume = alarmVolume;
        this.panicModeDuration = panicModeDuration;
    }

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
}
