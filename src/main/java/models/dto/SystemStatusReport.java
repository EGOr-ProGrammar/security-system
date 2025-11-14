package models.dto;

/**
 * Базовый класс для отчетов о состоянии систем безопасности.
 */
public abstract class SystemStatusReport {
    protected final String systemId;
    protected final String location;
    protected final String securityMode;
    protected final boolean isArmed;
    protected final int batteryLevel;
    protected final int signalStrength;

    public SystemStatusReport(String systemId, String location, String securityMode,
                              boolean isArmed, int batteryLevel, int signalStrength) {
        this.systemId = systemId;
        this.location = location;
        this.securityMode = securityMode;
        this.isArmed = isArmed;
        this.batteryLevel = batteryLevel;
        this.signalStrength = signalStrength;
    }

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
}
