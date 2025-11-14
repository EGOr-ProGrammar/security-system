package models;

import java.util.HashMap;
import java.util.Map;

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
        logEvent("Запуск самодиагностики автомобильной сигнализации");
        boolean shockTest = random.nextDouble() > 0.1;
        boolean tiltTest = random.nextDouble() > 0.15;
        boolean glassTest = random.nextDouble() > 0.1;
        boolean sirenTest = random.nextDouble() > 0.05;
        boolean result = shockTest && tiltTest && glassTest && sirenTest;
        logEvent("Самодиагностика автосигнализации: " + (result ? "УСПЕШНО" : "ОШИБКА"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, result ? EventType.SELF_TEST_SUCCESS : EventType.SELF_TEST_FAILED);
        }
        return result;
    }

    @Override
    public String simulateEmergency() {
        String[] emergencies = {"Удар по автомобилю", "Наклон автомобиля", "Разбитие стекла", "Обнаружение движения"};
        String emergency = emergencies[random.nextInt(emergencies.length)];
        return sendAlarm(emergency);
    }

    @Override
    public String getStatusReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("Датчик удара", shockSensorActive ? "Активен" : "Неактивен");
        report.put("Датчик наклона", tiltSensorActive ? "Активен" : "Неактивен");
        report.put("Датчик разбития стекла", glassBreakSensorActive ? "Активен" : "Неактивен");
        report.put("Дистанционный запуск", remoteStartEnabled ? "ВКЛ" : "ВЫКЛ");
        report.put("Громкость сигнала", alarmVolume);
        report.put("Длительность паники", panicModeDuration + " сек");
        report.put("Уровень батареи", batteryLevel + "%");
        report.put("Сила сигнала", signalStrength + "/5");
        return "Отчет автомобильной сигнализации:\n" + report.toString();
    }

    @Override
    public void calibrateSensors() {
        logEvent("Калибровка датчиков автомобильной сигнализации");
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CALIBRATION_COMPLETE);
        }
    }

    @Override
    public boolean checkConnectivity() {
        boolean connected = random.nextDouble() > 0.25;
        logEvent("Проверка связи с брелоком: " + (connected ? "ОК" : "ОШИБКА"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONNECTIVITY_CHECK, connected ? "OK" : "FAILED");
        }
        return connected;
    }

    public void activatePanicMode() {
        logEvent("Активирован режим паники");
        sendAlarm("Режим паники активирован");
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.PANIC_MODE_ACTIVATED);
        }
    }

    public void toggleShockSensor() {
        shockSensorActive = !shockSensorActive;
        logEvent("Датчик удара: " + (shockSensorActive ? "ВКЛ" : "ВЫКЛ"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Shock sensor: " + shockSensorActive);
        }
    }

    public void toggleTiltSensor() {
        tiltSensorActive = !tiltSensorActive;
        logEvent("Датчик наклона: " + (tiltSensorActive ? "ВКЛ" : "ВЫКЛ"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.SENSOR_TOGGLED, "Tilt sensor: " + tiltSensorActive);
        }
    }

    public void setAlarmVolume(String volume) {
        if (volume.equals("Тихая") || volume.equals("Средняя") || volume.equals("Громкая")) {
            alarmVolume = volume;
            logEvent("Громкость сигнала установлена: " + volume);
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Volume: " + volume);
            }
        }
    }

    public void simulateImpact() {
        if (isArmed && shockSensorActive) {
            sendAlarm("Обнаружен удар по автомобилю");
            if (csvLogger != null) {
                csvLogger.logEvent(systemId, EventType.IMPACT_DETECTED);
            }
        }
    }

    public void toggleRemoteStart() {
        remoteStartEnabled = !remoteStartEnabled;
        logEvent("Дистанционный запуск: " + (remoteStartEnabled ? "ВКЛ" : "ВЫКЛ"));
        if (csvLogger != null) {
            csvLogger.logEvent(systemId, EventType.CONFIG_CHANGED, "Remote start: " + remoteStartEnabled);
        }
    }

    // Геттеры
    public boolean isShockSensorActive() { return shockSensorActive; }
    public boolean isTiltSensorActive() { return tiltSensorActive; }
    public boolean isGlassBreakSensorActive() { return glassBreakSensorActive; }
    public boolean isRemoteStartEnabled() { return remoteStartEnabled; }
    public String getAlarmVolume() { return alarmVolume; }
    public int getPanicModeDuration() { return panicModeDuration; }

    // Сеттеры для TextFileParser
    public void setShockSensorActive(boolean active) {
        this.shockSensorActive = active;
        logEvent("Датчик удара: " + (active ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setTiltSensorActive(boolean active) {
        this.tiltSensorActive = active;
        logEvent("Датчик наклона: " + (active ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setGlassBreakSensorActive(boolean active) {
        this.glassBreakSensorActive = active;
        logEvent("Датчик разбития стекла: " + (active ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setRemoteStartEnabled(boolean enabled) {
        this.remoteStartEnabled = enabled;
        logEvent("Дистанционный запуск: " + (enabled ? "ВКЛ" : "ВЫКЛ"));
    }

    public void setPanicModeDuration(int duration) {
        if (duration > 0) {
            this.panicModeDuration = duration;
            logEvent("Длительность режима паники установлена: " + duration + " сек");
        }
    }
}
