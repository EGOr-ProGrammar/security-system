package views;

import config.ConfigManager;
import models.*;
import models.dto.*;

import java.util.List;

public class ConsoleView {
    private final ConfigManager config = ConfigManager.getInstance();

    public void displayHelp() {
        System.out.println("\n" + config.getString("help.usage"));
        System.out.println(config.getString("help.keys") + "\n");
    }

    public void displaySystemState(List<SecuritySystem> systems, String fileName) {
        System.out.println(config.getString("state.title"));
        System.out.println(config.getString("state.file") + " " + fileName);
        if(systems.isEmpty()) {
            System.out.println(config.getString("state.no.systems"));
            return;
        }

        for (int i = 0; i < systems.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, systems.get(i));
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayError(String error) {
        System.err.println(config.getString("error.prefix") + " " + error);
    }

    public void displayMainMenu(String currentFileName) {
        System.out.println(config.getString("menu.main.title"));
        System.out.println(config.getString("menu.main.add"));
        System.out.println(config.getString("menu.main.remove"));
        System.out.println(config.getString("menu.main.list"));
        System.out.println(config.getString("menu.main.edit"));
        System.out.println(config.getString("menu.main.operations"));
        System.out.println(config.getString("menu.main.files"));
        System.out.println(config.getString("menu.main.monitoring"));
        System.out.println(config.getString("menu.main.logging"));
        System.out.println(config.getString("menu.main.save.logs"));
        System.out.println(config.getString("menu.main.exit"));
        System.out.println(config.getString("menu.main.current.file") + " " + currentFileName);
    }

    public void displaySystemOperationsMenu(SecuritySystem system) {
        System.out.println(config.getString("menu.operations.title") + " " + system.getClass().getSimpleName() + " ===");
        System.out.println(config.getString("menu.operations.device") + " " + system);
        System.out.println(config.getString("menu.operations.arm"));
        System.out.println(config.getString("menu.operations.disarm"));
        System.out.println(config.getString("menu.operations.mode"));
        System.out.println(config.getString("menu.operations.test.alarm"));
        System.out.println(config.getString("menu.operations.self.test"));
        System.out.println(config.getString("menu.operations.report"));
        System.out.println(config.getString("menu.operations.calibrate"));
        System.out.println(config.getString("menu.operations.check.connection"));
        System.out.println(config.getString("menu.operations.event.log"));
        System.out.println(config.getString("menu.operations.specific"));
        System.out.println(config.getString("menu.operations.back"));
    }

    public void displayHomeAlarmMenu() {
        System.out.println(config.getString("menu.home.title"));
        System.out.println(config.getString("menu.home.door.sensors"));
        System.out.println(config.getString("menu.home.window.sensors"));
        System.out.println(config.getString("menu.home.sensitivity"));
        System.out.println(config.getString("menu.home.silent.mode"));
        System.out.println(config.getString("menu.home.simulate.intrusion"));
    }

    public void displayBiometricLockMenu() {
        System.out.println(config.getString("menu.biometric.title"));
        System.out.println(config.getString("menu.biometric.authenticate"));
        System.out.println(config.getString("menu.biometric.add.user"));
        System.out.println(config.getString("menu.biometric.lock"));
        System.out.println(config.getString("menu.biometric.unlock"));
        System.out.println(config.getString("menu.biometric.toggle.fingerprint"));
    }

    public void displayCarAlarmMenu() {
        System.out.println(config.getString("menu.car.title"));
        System.out.println(config.getString("menu.car.panic"));
        System.out.println(config.getString("menu.car.shock.sensor"));
        System.out.println(config.getString("menu.car.tilt.sensor"));
        System.out.println(config.getString("menu.car.alarm.volume"));
        System.out.println(config.getString("menu.car.simulate.impact"));
    }

    public void displayFileOperationsMenu() {
        System.out.println(config.getString("menu.files.title"));
        System.out.println(config.getString("menu.files.load"));
        System.out.println(config.getString("menu.files.change"));
    }

    public void displayEventLog(CSVLogger csvLogger, String systemId) {
        System.out.println(config.getString("log.title") + " " + systemId + " ===");
        List<String> logs = csvLogger.getLogsBySystemId(systemId, 50);
        if (logs.isEmpty()) {
            System.out.println(config.getString("log.empty"));
            return;
        }

        for (String log : logs) {
            String[] parts = log.split(",");
            if (parts.length >= 9) {
                System.out.printf("[%s] %s - %s%n", parts[0], parts[7], parts[8]);
            }
        }
    }

    public void displayEventLog(List<String> eventLog) {
        if (eventLog.isEmpty()) {
            displayMessage(config.getString("log.empty"));
        } else {
            displayMessage(config.getString("log.general.title"));
            eventLog.forEach(System.out::println);
        }
    }

    public void displayAllLogs(CSVLogger csvLogger) {
        System.out.println(config.getString("log.all.title"));
        List<String> logs = csvLogger.getRecentLogs(100);
        if (logs.isEmpty()) {
            System.out.println(config.getString("log.no.logs"));
            return;
        }

        for (String log : logs) {
            String[] parts = log.split(",");
            if (parts.length >= 9) {
                System.out.printf("[%s] %s - %s - %s%n", parts[0], parts[1], parts[7], parts[8]);
            }
        }
    }

    public void displayEmergencyEvent(EmergencyEvent event) {
        if (event == null) {
            displayError(config.getString("event.not.found"));
            return;
        }

        System.out.println(config.getString("event.title"));
        System.out.printf("%s %s%n", config.getString("event.system.id"), event.getSystemId());
        System.out.printf("%s %s%n", config.getString("event.system.type"), event.getSystemType());
        System.out.printf("%s %s%n", config.getString("event.type"), event.getEventType());
        System.out.printf("%s %s%n", config.getString("event.description"), event.getDescription());
        System.out.printf("%s %s%n", config.getString("event.timestamp"), event.getTimestamp());
        System.out.printf("%s %s%n", config.getString("event.response.required"),
                event.isRequiresResponse() ? config.getString("event.response.yes") : config.getString("event.response.no"));
    }

    public void displayStatusReport(SystemStatusReport report) {
        if (report == null) {
            displayError(config.getString("report.not.found"));
            return;
        }

        System.out.println(config.getString("report.title"));
        System.out.printf("%s %s%n", config.getString("report.system.id"), report.getSystemId());
        System.out.printf("%s %s%n", config.getString("report.location"), report.getLocation());
        System.out.printf("%s %s%n", config.getString("report.security.mode"), report.getSecurityMode());
        System.out.printf("%s %s%n", config.getString("report.armed"),
                report.isArmed() ? config.getString("report.armed.on") : config.getString("report.armed.off"));
        System.out.printf("%s %d%%%n", config.getString("report.battery"), report.getBatteryLevel());
        System.out.printf("%s %d/5%n", config.getString("report.signal"), report.getSignalStrength());

        if (report instanceof HomeAlarmStatusReport homeReport) {
            displayHomeAlarmDetails(homeReport);
        } else if (report instanceof BiometricLockStatusReport lockReport) {
            displayBiometricLockDetails(lockReport);
        } else if (report instanceof CarAlarmStatusReport carReport) {
            displayCarAlarmDetails(carReport);
        }
    }

    private void displayHomeAlarmDetails(HomeAlarmStatusReport report) {
        System.out.println(config.getString("report.home.sensors.title"));
        System.out.printf("%s %s%n", config.getString("report.home.door.sensors"),
                report.isDoorSensorsActive() ? config.getString("status.active") : config.getString("status.inactive"));
        System.out.printf("%s %s%n", config.getString("report.home.window.sensors"),
                report.isWindowSensorsActive() ? config.getString("status.active") : config.getString("status.inactive"));
        System.out.printf("%s %s%n", config.getString("report.home.motion.sensors"),
                report.isMotionSensorsActive() ? config.getString("status.active") : config.getString("status.inactive"));
        System.out.printf("%s %d/5%n", config.getString("report.home.sensitivity"), report.getSensitivityLevel());
        System.out.printf("%s %s%n", config.getString("report.home.silent.mode"),
                report.isSilentMode() ? config.getString("status.enabled") : config.getString("status.disabled"));
        System.out.printf("%s %s%n", config.getString("report.home.alarm.sound"), report.getAlarmSound());
    }

    private void displayBiometricLockDetails(BiometricLockStatusReport report) {
        System.out.println(config.getString("report.biometric.title"));
        System.out.printf("%s %d %s%n", config.getString("report.biometric.authorized.users"),
                report.getAuthorizedUsersCount(), config.getString("report.biometric.users.suffix"));
        System.out.printf("%s %d%n", config.getString("report.biometric.failed.attempts"), report.getFailedAttempts());
        System.out.printf("%s %s%n", config.getString("report.biometric.fingerprint"),
                report.isFingerprintEnabled() ? config.getString("status.on") : config.getString("status.off"));
        System.out.printf("%s %s%n", config.getString("report.biometric.face.recognition"),
                report.isFaceRecognitionEnabled() ? config.getString("status.enabled") : config.getString("status.disabled"));
        System.out.printf("%s %s%n", config.getString("report.biometric.lock.status"), report.getLockStatus());
        System.out.printf("%s %d %s%n", config.getString("report.biometric.auto.lock"),
                report.getAutoLockDelay(), config.getString("report.biometric.seconds.suffix"));
    }

    private void displayCarAlarmDetails(CarAlarmStatusReport report) {
        System.out.println(config.getString("report.car.title"));
        System.out.printf("%s %s%n", config.getString("report.car.shock.sensor"),
                report.isShockSensorActive() ? config.getString("status.active") : config.getString("status.inactive"));
        System.out.printf("%s %s%n", config.getString("report.car.tilt.sensor"),
                report.isTiltSensorActive() ? config.getString("status.active") : config.getString("status.inactive"));
        System.out.printf("%s %s%n", config.getString("report.car.glass.break.sensor"),
                report.isGlassBreakSensorActive() ? config.getString("status.active") : config.getString("status.inactive"));
        System.out.printf("%s %s%n", config.getString("report.car.remote.start"),
                report.isRemoteStartEnabled() ? config.getString("status.enabled") : config.getString("status.disabled"));
        System.out.printf("%s %s%n", config.getString("report.car.volume"), report.getAlarmVolume());
        System.out.printf("%s %d %s%n", config.getString("report.car.panic.mode"),
                report.getPanicModeDuration(), config.getString("report.biometric.seconds.suffix"));
    }

    public void waitForEnter() {
        ConsoleInputHandler.waitForEnter();
    }

    public void displayPrompt(String prompt) {
        System.out.print(prompt + ": ");
    }
}
