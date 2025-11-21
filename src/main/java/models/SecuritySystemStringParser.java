package models;

import java.util.*;

public class SecuritySystemStringParser {
    public static SecuritySystem parse(String data) {
        if (data == null || data.isEmpty()) return null;

        String[] parts = data.trim().split("\\s+");
        if (parts.length < 2) return null;

        String type = parts[0].toLowerCase();
        Map<String, String> props = new HashMap<>();
        for (int i = 1; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2) props.put(kv[0].toLowerCase(), kv[1]);
        }

        switch(type) {
            case "homealarmsystem": {
                HomeAlarmSystem home = new HomeAlarmSystem(
                        props.getOrDefault("id", "unknown"),
                        props.getOrDefault("location", "unknown")
                );
                home.setSecurityMode(props.getOrDefault("securitymode", "normal"));
                home.armSystem();
                home.setBatteryLevel(parseInt(props.getOrDefault("batterylevel", "80")));
                home.setSignalStrength(parseInt(props.getOrDefault("signalstrength", "1")));
                home.setDoorSensorsActive(Boolean.parseBoolean(props.getOrDefault("doorsensorsactive", "true")));
                home.setWindowSensorsActive(Boolean.parseBoolean(props.getOrDefault("windowsensorsactive", "true")));
                home.setMotionSensorsActive(Boolean.parseBoolean(props.getOrDefault("motionsensorsactive", "true")));
                home.setSensitivityLevel(parseInt(props.getOrDefault("sensitivitylevel", "3")));
                home.setSilentMode(Boolean.parseBoolean(props.getOrDefault("silentmode", "false")));
                home.setAlarmSound(props.getOrDefault("alarmsound", "default"));
                return home;
            }
            case "biometriclock": {
                BiometricLock bio = new BiometricLock(
                        props.getOrDefault("id", "unknown"),
                        props.getOrDefault("location", "unknown")
                );
                bio.setSecurityMode(props.getOrDefault("securitymode", "standard"));
                bio.armSystem();
                bio.setBatteryLevel(parseInt(props.getOrDefault("batterylevel", "80")));
                bio.setSignalStrength(parseInt(props.getOrDefault("signalstrength", "1")));
                bio.setFailedAttempts(parseInt(props.getOrDefault("failedattempts", "0")));
                bio.setFingerprintEnabled(Boolean.parseBoolean(props.getOrDefault("fingerprintenabled", "true")));
                bio.setFaceRecognitionEnabled(Boolean.parseBoolean(props.getOrDefault("facerecognitionenabled", "true")));
                bio.setLockStatus(props.getOrDefault("lockstatus", "locked"));
                bio.setAutoLockDelay(parseInt(props.getOrDefault("autolockdelay", "10")));
                return bio;
            }
            case "caralarmsystem": {
                CarAlarmSystem car = new CarAlarmSystem(
                        props.getOrDefault("id", "unknown"),
                        props.getOrDefault("location", "unknown")
                );
                car.setSecurityMode(props.getOrDefault("securitymode", "standard"));
                car.armSystem();
                car.setBatteryLevel(parseInt(props.getOrDefault("batterylevel", "80")));
                car.setSignalStrength(parseInt(props.getOrDefault("signalstrength", "1")));
                car.setShockSensorActive(Boolean.parseBoolean(props.getOrDefault("shocksensoractive", "true")));
                car.setTiltSensorActive(Boolean.parseBoolean(props.getOrDefault("tiltsensoractive", "true")));
                car.setGlassBreakSensorActive(Boolean.parseBoolean(props.getOrDefault("glassbreaksensoractive", "true")));
                return car;
            }
        }
        return null;
    }

    // защитный parse для int, если будет не число — вернёт дефолт
    private static int parseInt(String value) {
        try { return Integer.parseInt(value); }
        catch (Exception e) { return 0; }
    }
}
