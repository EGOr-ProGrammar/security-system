package controllers;

import views.ConsoleView;
import models.*;
import views.ConsoleInputHandler;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Обеспечивает исполнение команд, введенных пользователем.
 */
public class InteractiveModeController {
    private final SecuritySystemController systemController;
    private final ConsoleView view;
    private boolean continuousMonitoring = false;
    private boolean csvLogging = false;

    public InteractiveModeController(SecuritySystemController systemController, ConsoleView view) {
        this.systemController = systemController;
        this.view = view;
        systemController.loadSystemsFromFile(systemController.getCurrentFileName(), false);
    }

    public void run() {
        view.displayMessage("=== ИНТЕРАКТИВНЫЙ РЕЖИМ УПРАВЛЕНИЯ БЕЗОПАСНОСТЬЮ ===");
        while (true) {
            view.displayMainMenu(systemController.getCurrentFileName());
            int choice = ConsoleInputHandler.getIntInput("Выберите пункт меню: ", 0, 9);
            switch (choice) {
                case 1 -> addSystem();
                case 2 -> removeSystem();
                case 3 -> listSystems();
                case 4 -> modifySystem();
                case 5 -> systemOperations();
                case 6 -> fileOperations();
                case 7 -> continuousMonitoring();
                case 8 -> csvLogging();
                case 9 -> saveAllEventLogsToFile();
                case 0 -> {
                    view.displayMessage("Выход из программы.");
                    systemController.close();
                    return;
                }
            }
        }
    }

    private void addSystem() {
        List<String> deviceTypes = Arrays.asList(
                "Домашняя сигнализация", "Биометрический замок", "Автомобильная сигнализация"
        );
        int typeIndex = ConsoleInputHandler.getChoiceFromList("Выберите тип системы:", deviceTypes);
        String id = ConsoleInputHandler.getStringInput("Введите ID системы: ");
        String location = ConsoleInputHandler.getStringInput("Введите местоположение: ");
        SecuritySystem newSystem = switch (typeIndex) {
            case 0 -> new HomeAlarmSystem(id, location);
            case 1 -> new BiometricLock(id, location);
            case 2 -> new CarAlarmSystem(id, location);
            default -> throw new IllegalStateException("Неверный тип системы");
        };
        systemController.addSystem(newSystem);
        view.displayMessage("Система успешно добавлена!");
        view.waitForEnter();
    }

    private void removeSystem() {
        listSystems();
        if (systemController.getAllSystems().isEmpty()) return;
        int index = ConsoleInputHandler.getIntInput("Введите номер системы для удаления: ",
                1, systemController.getAllSystems().size()) - 1;
        if (ConsoleInputHandler.getConfirmation("Вы уверены?")) {
            if (systemController.removeSystem(index)) {
                view.displayMessage("Система удалена!");
            } else {
                view.displayError("Ошибка при удалении системы");
            }
        }
        view.waitForEnter();
    }

    private void listSystems() {
        view.displaySystemState(systemController.getAllSystems(), systemController.getCurrentFileName());
        view.waitForEnter();
    }

    private void modifySystem() {
        listSystems();
        if (systemController.getAllSystems().isEmpty()) return;
        int index = ConsoleInputHandler.getIntInput("Введите номер системы: ",
                1, systemController.getAllSystems().size()) - 1;
        SecuritySystem system = systemController.getSystem(index);
        if (system != null) {
            String newLocation = ConsoleInputHandler.getStringInput("Новое местоположение: ");
            system.setLocation(newLocation);
            List<String> modes = Arrays.asList("Отключено", "Дома", "Отсутствие");
            int modeIndex = ConsoleInputHandler.getChoiceFromList("Выберите режим безопасности:", modes);
            system.setSecurityMode(modes.get(modeIndex));
            view.displayMessage("Система изменена!");
        }
        view.waitForEnter();
    }

    private void systemOperations() {
        listSystems();
        if (systemController.getAllSystems().isEmpty()) return;
        int index = ConsoleInputHandler.getIntInput("Введите номер системы: ",
                1, systemController.getAllSystems().size()) - 1;
        SecuritySystem system = systemController.getSystem(index);
        if (system != null) {
            systemOperationsMenu(system);
        }
    }

    private void systemOperationsMenu(SecuritySystem system) {
        while (true) {
            view.displaySystemOperationsMenu(system);
            int choice = ConsoleInputHandler.getIntInput("Выберите действие: ", 0, 10);
            if (choice == 0) break;
            executeSystemOperation(system, choice);
            system.updateSensorStatus();
            view.waitForEnter();
        }
    }

    private void executeSystemOperation(SecuritySystem system, int operation) {
        switch (operation) {
            case 1 -> {
                system.armSystem();
                view.displayMessage("Режим охраны установлен");
                // Логирование теперь через enum
            }
            case 2 -> {
                system.disarmSystem();
                view.displayMessage("Режим охраны снят");
            }
            case 3 -> {
                List<String> modes = Arrays.asList("Отключено", "Дома", "Отсутствие");
                int modeIndex = ConsoleInputHandler.getChoiceFromList("Выберите режим безопасности:", modes);
                system.setSecurityMode(modes.get(modeIndex));
                view.displayMessage("Режим безопасности установлен");
            }
            case 4 -> {
                String alarm = system.simulateEmergency();
                view.displayMessage(alarm);
            }
            case 5 -> {
                boolean testResult = system.performSelfTest();
                view.displayMessage("Самодиагностика: " + (testResult ? "УСПЕШНО" : "ОШИБКА"));
            }
            case 6 -> view.displayMessage(system.getStatusReport());
            case 7 -> {
                system.calibrateSensors();
                view.displayMessage("Сенсоры откалиброваны");
            }
            case 8 -> {
                boolean connectivity = system.checkConnectivity();
                view.displayMessage("Подключение: " + (connectivity ? "УСПЕШНО" : "ОШИБКА"));
            }
            case 9 -> view.displayEventLog(system.getEventLog());
            case 10 -> showSpecificFunctions(system);
        }
    }

    private void showSpecificFunctions(SecuritySystem system) {
        if (system instanceof HomeAlarmSystem homeAlarm) {
            homeAlarmSpecificOperations(homeAlarm);
        } else if (system instanceof BiometricLock biometricLock) {
            biometricLockSpecificOperations(biometricLock);
        } else if (system instanceof CarAlarmSystem carAlarm) {
            carAlarmSpecificOperations(carAlarm);
        }
    }

    private void homeAlarmSpecificOperations(HomeAlarmSystem alarm) {
        view.displayHomeAlarmMenu();
        int choice = ConsoleInputHandler.getIntInput("Выберите действие: ", 1, 5);
        switch (choice) {
            case 1 -> {
                alarm.toggleDoorSensors();
                view.displayMessage("Датчики дверей переключены");
            }
            case 2 -> {
                alarm.toggleWindowSensors();
                view.displayMessage("Датчики окон переключены");
            }
            case 3 -> {
                int level = ConsoleInputHandler.getIntInput("Уровень чувствительности (1-5): ", 1, 5);
                alarm.setSensitivity(level);
                view.displayMessage("Чувствительность установлена");
            }
            case 4 -> {
                alarm.toggleSilentMode();
                view.displayMessage("Тихий режим переключен");
            }
            case 5 -> {
                alarm.simulateIntrusion();
                view.displayMessage("Вторжение сымитировано");
            }
        }
    }

    private void biometricLockSpecificOperations(BiometricLock lock) {
        view.displayBiometricLockMenu();
        int choice = ConsoleInputHandler.getIntInput("Выберите действие: ", 1, 5);
        switch (choice) {
            case 1 -> {
                String fingerprint = ConsoleInputHandler.getStringInput("Введите ID отпечатка: ");
                boolean authResult = lock.authenticateUser(fingerprint);
                view.displayMessage("Аутентификация: " + (authResult ? "УСПЕШНО" : "ОШИБКА"));
            }
            case 2 -> {
                String id = ConsoleInputHandler.getStringInput("Введите ID отпечатка: ");
                String name = ConsoleInputHandler.getStringInput("Введите имя пользователя: ");
                lock.addUser(id, name);
                view.displayMessage("Пользователь добавлен");
            }
            case 3 -> {
                lock.lockDoor();
                view.displayMessage("Дверь заблокирована");
            }
            case 4 -> {
                lock.unlockDoor();
                view.displayMessage("Дверь открыта");
            }
            case 5 -> {
                lock.toggleFingerprintScanner();
                view.displayMessage("Сканер отпечатков переключен");
            }
        }
    }

    private void carAlarmSpecificOperations(CarAlarmSystem carAlarm) {
        view.displayCarAlarmMenu();
        int choice = ConsoleInputHandler.getIntInput("Выберите действие: ", 1, 5);
        switch (choice) {
            case 1 -> {
                carAlarm.activatePanicMode();
                view.displayMessage("Режим паники активирован");
            }
            case 2 -> {
                carAlarm.toggleShockSensor();
                view.displayMessage("Датчик удара переключен");
            }
            case 3 -> {
                carAlarm.toggleTiltSensor();
                view.displayMessage("Датчик наклона переключен");
            }
            case 4 -> {
                List<String> volumes = Arrays.asList("Тихая", "Средняя", "Громкая");
                int volumeIndex = ConsoleInputHandler.getChoiceFromList("Выберите громкость:", volumes);
                carAlarm.setAlarmVolume(volumes.get(volumeIndex));
                view.displayMessage("Громкость установлена");
            }
            case 5 -> {
                carAlarm.simulateImpact();
                view.displayMessage("Удар сымитирован");
            }
        }
    }

    private void fileOperations() {
        view.displayFileOperationsMenu();
        int choice = ConsoleInputHandler.getIntInput("Выберите действие: ", 1, 2);
        switch (choice) {
            case 1 -> {
                String fileName = ConsoleInputHandler.getStringInput("Имя файла: ");
                if (systemController.loadSystemsFromFile(fileName, false)) {
                    view.displayMessage("Системы загружены!");
                } else {
                    view.displayError("Ошибка загрузки");
                }
            }
            case 2 -> {
                String newFile = ConsoleInputHandler.getStringInput("Новое имя файла: ");
                systemController.setFileName(newFile);
                view.displayMessage("Файл изменен!");
            }
        }
        view.waitForEnter();
    }

    private void continuousMonitoring() {
        int interval = ConsoleInputHandler.getIntInput("Интервал (секунды): ", 1, 3600);
        continuousMonitoring = true;
        view.displayMessage("Мониторинг запущен. Нажмите Enter для остановки.");
        Thread monitorThread = new Thread(() -> {
            while (continuousMonitoring) {
                view.displaySystemState(systemController.getAllSystems(), systemController.getCurrentFileName());
                try {
                    Thread.sleep(interval * 1000L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        monitorThread.start();
        view.waitForEnter();
        continuousMonitoring = false;
        view.displayMessage("Мониторинг остановлен.");
    }

    private void csvLogging() {
        int interval = ConsoleInputHandler.getIntInput("Интервал логирования (секунды): ", 1, 3600);
        systemController.setCSVLogInterval(interval);
        csvLogging = true;
        view.displayMessage("Логирование запущено. Нажмите Enter для остановки.");
        Thread loggingThread = new Thread(() -> {
            while (csvLogging) {
                systemController.logAllSystemsState();
                try {
                    Thread.sleep(interval * 1000L);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        loggingThread.start();
        view.waitForEnter();
        csvLogging = false;
        view.displayMessage("Логирование остановлено.");
    }

    private void saveAllEventLogsToFile() {
        if (systemController.getAllSystems().isEmpty()) {
            view.displayMessage("Нет систем для сохранения логов.");
            view.waitForEnter();
            return;
        }
        String filename = ConsoleInputHandler.getStringInput("Введите имя файла для сохранения (например: all_event_logs.txt): ");
        String description = ConsoleInputHandler.getStringInput("Введите общее описание для логов: ");
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, true))) {
            writer.println("=== ОБЩИЙ ЖУРНАЛ СОБЫТИЙ СИСТЕМ БЕЗОПАСНОСТИ ===");
            writer.println("Описание: " + description);
            writer.println("Время экспорта: " + java.time.LocalDateTime.now());
            writer.println("Количество устройств: " + systemController.getAllSystems().size());
            writer.println("===============================================");
            writer.println();
            int savedCount = 0;
            for (SecuritySystem system : systemController.getAllSystems()) {
                String deviceType = system.getClass().getSimpleName();
                String deviceName = system.getSystemId() + " (" + system.getLocation() + ")";
                writer.println("--- УСТРОЙСТВО: " + deviceName + " ---");
                writer.println("Тип: " + deviceType);
                writer.println("Режим безопасности: " + system.getSecurityMode());
                writer.println("Охрана: " + (system.isArmed() ? "ВКЛ" : "ВЫКЛ"));
                writer.println("Уровень батареи: " + system.getBatteryLevel() + "%");
                writer.println("Сила сигнала: " + system.getSignalStrength() + "/5");
                writer.println();
                writer.println("События:");
                writer.println("--------");
                List<String> eventLog = system.getEventLog();
                if (eventLog.isEmpty()) {
                    writer.println("Журнал событий пуст");
                } else {
                    for (String event : eventLog) {
                        writer.println(event);
                    }
                }
                writer.println();
                writer.println("----------------------------------------");
                writer.println();
                savedCount++;
            }
            writer.println("=== ЭКСПОРТ ЗАВЕРШЕН ===");
            writer.println("Успешно сохранено: " + savedCount + " устройств");
            writer.println("===============================================");
            view.displayMessage("УСПЕШНО сохранены журналы событий " + savedCount + " устройств в файл: " + filename);
        } catch (java.io.IOException e) {
            view.displayError("ОШИБКА при сохранении логов в файл: " + e.getMessage());
        }
        view.waitForEnter();
    }
}
