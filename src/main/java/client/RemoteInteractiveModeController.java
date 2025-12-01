package client;

import config.ConfigManager;
import models.dto.EmergencyEvent;
import models.dto.SystemStatusReport;
import views.ConsoleView;
import models.*;
import views.ConsoleInputHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Адаптер для InteractiveModeController, работающий с удаленным сервером
 */
public class RemoteInteractiveModeController {
    private final RemoteSecuritySystemController remoteController;
    private final ConsoleView view;
    private final ConfigManager config = ConfigManager.getInstance();
    private boolean continuousMonitoring = false;
    private boolean csvLogging = false;

    public RemoteInteractiveModeController(RemoteSecuritySystemController remoteController, ConsoleView view) {
        this.remoteController = remoteController;
        this.view = view;

        // Загрузить имя текущего файла с сервера
        try {
            String fileName = remoteController.getCurrentFileName();
            if (fileName != null && !fileName.isEmpty()) {
                System.out.println("Текущий файл на сервере: " + fileName);
            }
        } catch (IOException e) {
            System.err.println("Предупреждение: не удалось получить имя файла с сервера");
        }
    }

    public void run() {
        view.displayMessage(config.getString("interactive.title"));

        while (true) {
            try {
                String currentFile = remoteController.getCurrentFileName();
                view.displayMainMenu(currentFile);

                int choice = ConsoleInputHandler.getIntInput(config.getString("prompt.choose") + " ", 0, 8);

                // Если получили 0, возможно программа завершается
                if (choice == 0) {
                    view.displayMessage(config.getString("menu.main.exit"));
                    remoteController.close();
                    return;
                }

                switch (choice) {
                    case 1 -> addSystem();
                    case 2 -> removeSystem();
                    case 3 -> listSystems();
                    case 4 -> modifySystem();
                    case 5 -> systemOperations();
                    case 6 -> fileOperations();
                    case 7 -> continuousMonitoring();
                    case 8 -> csvLogging();
                }

            } catch (IOException e) {
                view.displayError("Ошибка связи с сервером: " + e.getMessage());
                view.waitForEnter();
                // Выходим при потере связи
                break;
            } catch (Exception e) {
                // При любой критической ошибке выходим
                if (!e.getMessage().contains("No line found")) {
                    view.displayError("Критическая ошибка: " + e.getMessage());
                }
                break;
            }
        }

        // Корректное завершение
        try {
            remoteController.close();
        } catch (Exception ignored) {

        }
    }

    private void addSystem() throws IOException {
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

        remoteController.addSystem(newSystem);
        view.displayMessage("Система успешно добавлена!");
        view.waitForEnter();
    }

    private void removeSystem() throws IOException {
        listSystems();
        List<SecuritySystem> systems = remoteController.getAllSystems();
        if (systems.isEmpty()) return;

        int index = ConsoleInputHandler.getIntInput("Введите номер системы для удаления: ",
                1, systems.size()) - 1;

        if (ConsoleInputHandler.getConfirmation("Вы уверены?")) {
            if (remoteController.removeSystem(index)) {
                view.displayMessage("Система удалена!");
            } else {
                view.displayError("Ошибка при удалении системы");
            }
        }
        view.waitForEnter();
    }

    private void listSystems() throws IOException {
        List<SecuritySystem> systems = remoteController.getAllSystems();
        String currentFile = remoteController.getCurrentFileName();
        view.displaySystemState(systems, currentFile);
        view.waitForEnter();
    }

    private void modifySystem() throws IOException {
        listSystems();
        List<SecuritySystem> systems = remoteController.getAllSystems();
        if (systems.isEmpty()) return;

        int index = ConsoleInputHandler.getIntInput("Введите номер системы: ",
                1, systems.size()) - 1;

        SecuritySystem system = remoteController.getSystem(index);
        if (system != null) {
            String newLocation = ConsoleInputHandler.getStringInput("Новое местоположение: ");
            system.setLocation(newLocation);

            List<String> modes = Arrays.asList(
                    config.getString("mode.off"),
                    config.getString("mode.home"),
                    config.getString("mode.away")
            );

            int modeIndex = ConsoleInputHandler.getChoiceFromList("Выберите режим безопасности:", modes);

            remoteController.setSecurityMode(index, modes.get(modeIndex));
            view.displayMessage("Система изменена!");
        }
        view.waitForEnter();
    }

    private void systemOperations() throws IOException {
        listSystems();
        List<SecuritySystem> systems = remoteController.getAllSystems();
        if (systems.isEmpty()) return;

        int index = ConsoleInputHandler.getIntInput("Введите номер системы: ",
                1, systems.size()) - 1;

        SecuritySystem system = remoteController.getSystem(index);
        if (system != null) {
            systemOperationsMenu(system, index);
        }
    }

    private void systemOperationsMenu(SecuritySystem system, int index) throws IOException {
        while (true) {
            view.displaySystemOperationsMenu(system);
            int choice = ConsoleInputHandler.getIntInput(config.getString("prompt.choose") + " ", 0, 10);

            if (choice == 0) break;

            executeSystemOperation(system, index, choice);

            // Обновляем систему с сервера
            system = remoteController.getSystem(index);
            view.waitForEnter();
        }
    }

    private void executeSystemOperation(SecuritySystem system, int index, int operation) throws IOException {
        switch (operation) {
            case 1 -> {
                remoteController.armSystem(index);
                view.displayMessage("Режим охраны установлен");
            }
            case 2 -> {
                remoteController.disarmSystem(index);
                view.displayMessage("Режим охраны снят");
            }
            case 3 -> {
                List<String> modes = Arrays.asList(
                        config.getString("mode.off"),
                        config.getString("mode.home"),
                        config.getString("mode.away")
                );
                int modeIndex = ConsoleInputHandler.getChoiceFromList("Выберите режим безопасности:", modes);
                remoteController.setSecurityMode(index, modes.get(modeIndex));
                view.displayMessage("Режим безопасности установлен");
            }
            case 4 -> {
                EmergencyEvent emergencyEvent = remoteController.simulateEmergency(index);
                view.displayEmergencyEvent(emergencyEvent);
            }
            case 5 -> {
                boolean testResult = remoteController.performSelfTest(index);
                view.displayMessage("Самодиагностика: " + (testResult ? "УСПЕШНО" : "ОШИБКА"));
            }
            case 6 -> {
                SystemStatusReport report = remoteController.getStatusReport(index);
                view.displayStatusReport(report);
            }
            case 7 -> {
                remoteController.calibrateSensors(index);
                view.displayMessage("Сенсоры откалиброваны");
            }
            case 8 -> {
                boolean connectivity = remoteController.checkConnectivity(index);
                view.displayMessage("Подключение: " + (connectivity ? "УСПЕШНО" : "ОШИБКА"));
            }
            case 9 -> {
                List<String> logs = remoteController.getCsvLogs(system.getSystemId(), 50);
                displayEventLog(logs, system.getSystemId());
            }
            case 10 -> {
                view.displayMessage("Специфичные функции пока не поддерживаются в удаленном режиме");
            }
        }
    }

    private void displayEventLog(List<String> logs, String systemId) {
        System.out.println("=== Журнал событий системы " + systemId + " ===");
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

    private void fileOperations() throws IOException {
        view.displayFileOperationsMenu();
        int choice = ConsoleInputHandler.getIntInput(config.getString("prompt.choose") + " ", 1, 2);

        switch (choice) {
            case 1 -> {
                String fileName = ConsoleInputHandler.getStringInput("Имя файла: ");
                if (remoteController.loadSystemsFromFile(fileName, false)) {
                    view.displayMessage("Системы загружены!");
                } else {
                    view.displayError("Ошибка загрузки");
                }
            }
            case 2 -> {
                String newFile = ConsoleInputHandler.getStringInput("Новое имя файла: ");
                remoteController.setFileName(newFile);
                view.displayMessage("Файл изменен!");
            }
        }
        view.waitForEnter();
    }

    private void continuousMonitoring() throws IOException {
        int interval = ConsoleInputHandler.getIntInput("Интервал (секунды): ", 1, 3600);
        continuousMonitoring = true;
        view.displayMessage("Мониторинг запущен. Нажмите Enter для остановки.");

        Thread monitorThread = new Thread(() -> {
            while (continuousMonitoring) {
                try {
                    List<SecuritySystem> systems = remoteController.getAllSystems();
                    String currentFile = remoteController.getCurrentFileName();
                    view.displaySystemState(systems, currentFile);
                    Thread.sleep(interval * 1000L);
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    view.displayError("Ошибка связи: " + e.getMessage());
                    break;
                }
            }
        });

        monitorThread.start();
        view.waitForEnter();
        continuousMonitoring = false;
        view.displayMessage("Мониторинг остановлен.");
    }

    private void csvLogging() throws IOException {
        int interval = ConsoleInputHandler.getIntInput("Интервал логирования (секунды): ", 1, 3600);
        remoteController.setCSVLogInterval(interval);
        csvLogging = true;
        view.displayMessage("Логирование запущено. Нажмите Enter для остановки.");

        Thread loggingThread = new Thread(() -> {
            while (csvLogging) {
                try {
                    remoteController.logAllSystemsState();
                    Thread.sleep(interval * 1000L);
                } catch (InterruptedException e) {
                    break;
                } catch (IOException e) {
                    view.displayError("Ошибка связи: " + e.getMessage());
                    break;
                }
            }
        });

        loggingThread.start();
        view.waitForEnter();
        csvLogging = false;
        view.displayMessage("Логирование остановлено.");
    }
}
