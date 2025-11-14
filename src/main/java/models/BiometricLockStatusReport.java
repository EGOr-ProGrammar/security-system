package models;

/**
 * Отчет о состоянии биометрического замка.
 */
public class BiometricLockStatusReport extends SystemStatusReport {
    private final int authorizedUsersCount;
    private final int failedAttempts;
    private final boolean fingerprintEnabled;
    private final boolean faceRecognitionEnabled;
    private final String lockStatus;
    private final int autoLockDelay;

    public BiometricLockStatusReport(String systemId, String location, String securityMode,
                                     boolean isArmed, int batteryLevel, int signalStrength,
                                     int authorizedUsersCount, int failedAttempts,
                                     boolean fingerprintEnabled, boolean faceRecognitionEnabled,
                                     String lockStatus, int autoLockDelay) {
        super(systemId, location, securityMode, isArmed, batteryLevel, signalStrength);
        this.authorizedUsersCount = authorizedUsersCount;
        this.failedAttempts = failedAttempts;
        this.fingerprintEnabled = fingerprintEnabled;
        this.faceRecognitionEnabled = faceRecognitionEnabled;
        this.lockStatus = lockStatus;
        this.autoLockDelay = autoLockDelay;
    }

    public int getAuthorizedUsersCount() {
        return authorizedUsersCount;
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
}
