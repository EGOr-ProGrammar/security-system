package controllers;

import client.RemoteSecuritySystemController;
import views.ConsoleView;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Управляет считыванием и исполнением флагов из аргументов командной строки
 * для клиентской части приложения
 */
public class ClientCommandLineController {
    private final RemoteSecuritySystemController remoteController;
    private final ConsoleView view;
    private final ScheduledExecutorService scheduler;
    private boolean isMonitoring = false;
    private boolean isLogging = false;

    public ClientCommandLineController(RemoteSecuritySystemController remoteController, ConsoleView view) {
        this.remoteController = remoteController;
        this.view = view;
        this.scheduler = Executors.newScheduledThreadPool(2);
    }

    /**
     * Обрабатывает аргументы командной строки
     * @param args аргументы из main()
     * @return true если нужно завершить программу, false если запустить интерактивный режим
     */
    public boolean processArgs(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-h":
                    case "--help":
                        view.displayHelp();
                        return true;

                    case "-f":
                    case "--file":
                        if (i + 1 < args.length) {
                            String fileName = args[++i];
                            try {
                                if (remoteController.loadSystemsFromFile(fileName, false)) {
                                    view.displayMessage("Системы успешно загружены из файла: " + fileName);
                                } else {
                                    view.displayError("Ошибка загрузки файла: " + fileName);
                                }
                            } catch (IOException e) {
                                view.displayError("Ошибка связи с сервером: " + e.getMessage());
                            }
                        } else {
                            view.displayError("Ключ -f требует имя файла");
                            return true;
                        }
                        break;

                    case "-s":
                    case "--state":
                        // Разовый вывод информации о состоянии объектов
                        try {
                            String currentFile = remoteController.getCurrentFileName();
                            if (currentFile == null || currentFile.isEmpty()) {
                                view.displayError("Файл не установлен на сервере");
                            } else {
                                remoteController.loadSystemsFromFile(currentFile, false);
                            }
                            view.displaySystemState(
                                    remoteController.getAllSystems(),
                                    remoteController.getCurrentFileName()
                            );
                        } catch (IOException e) {
                            view.displayError("Ошибка связи с сервером: " + e.getMessage());
                        }
                        return true;

                    case "-c":
                    case "--continuous":
                        // Циклический вывод информации о состоянии объектов
                        if (i + 1 < args.length) {
                            int interval = Integer.parseInt(args[++i]);
                            if (interval <= 0) {
                                view.displayError("Интервал должен быть положительным числом");
                                return true;
                            }

                            startContinuousMonitoring(interval);
                            waitForInterrupt();
                        } else {
                            view.displayError("Ключ -c требует интервал в секундах");
                        }
                        return true;

                    case "-l":
                    case "--log":
                        // Выгрузка данных в CSV с настройкой периода
                        if (i + 1 < args.length) {
                            int interval = Integer.parseInt(args[++i]);
                            if (interval <= 0) {
                                view.displayError("Интервал должен быть положительным числом");
                                return true;
                            }

                            startCSVLogging(interval);
                            waitForInterrupt();
                            return true;
                        } else {
                            view.displayError("Ключ -l требует интервал в секундах");
                            return true;
                        }

                    case "-i":
                    case "--interactive":
                        // Запуск интерактивного режима
                        return false;

                    case "-r":
                    case "--replace":
                        // Замена файла
                        if (i + 1 < args.length) {
                            String newFile = args[++i];
                            try {
                                remoteController.setFileName(newFile);
                                view.displayMessage("Текущий файл изменен на: " + newFile);
                            } catch (IOException e) {
                                view.displayError("Ошибка связи с сервером: " + e.getMessage());
                            }
                        } else {
                            view.displayError("Ключ -r требует имя файла");
                            return true;
                        }
                        break;

                    default:
                        view.displayError("Неизвестный ключ: " + args[i]);
                        view.displayHelp();
                        return true;
                }
            }

            // Если аргументы были обработаны без ключа -i, показываем состояние и выходим
            if (args.length > 0) {
                try {
                    view.displaySystemState(
                            remoteController.getAllSystems(),
                            remoteController.getCurrentFileName()
                    );
                } catch (IOException e) {
                    view.displayError("Ошибка связи с сервером: " + e.getMessage());
                }
                return true;
            }

            return false;

        } catch (NumberFormatException e) {
            view.displayError("Ошибка: интервал должен быть числом");
            view.displayHelp();
            return true;
        } catch (Exception e) {
            view.displayError("Ошибка обработки аргументов: " + e.getMessage());
            view.displayHelp();
            return true;
        }
    }

    private void startContinuousMonitoring(int interval) {
        isMonitoring = true;
        scheduler.scheduleAtFixedRate(() -> {
            if (isMonitoring) {
                try {
                    System.out.println("\n========================================");
                    System.out.println("Время: " + java.time.LocalDateTime.now());
                    view.displaySystemState(
                            remoteController.getAllSystems(),
                            remoteController.getCurrentFileName()
                    );
                    System.out.println("========================================");
                } catch (IOException e) {
                    view.displayError("Ошибка связи с сервером: " + e.getMessage());
                    isMonitoring = false;
                }
            }
        }, 0, interval, TimeUnit.SECONDS);

        view.displayMessage("Непрерывный мониторинг запущен. Интервал: " + interval + " сек.");
        view.displayMessage("Нажмите Ctrl+C для остановки.");
    }

    private void startCSVLogging(int interval) {
        isLogging = true;

        try {
            remoteController.setCSVLogInterval(interval);
        } catch (IOException e) {
            view.displayError("Ошибка установки интервала: " + e.getMessage());
            return;
        }

        scheduler.scheduleAtFixedRate(() -> {
            if (isLogging) {
                try {
                    remoteController.logAllSystemsState();
                    System.out.println("[" + java.time.LocalDateTime.now() + "] Записано " +
                            remoteController.getSystemCount() + " систем в CSV");
                } catch (IOException e) {
                    view.displayError("Ошибка связи с сервером: " + e.getMessage());
                    isLogging = false;
                }
            }
        }, 0, interval, TimeUnit.SECONDS);

        view.displayMessage("Логирование в CSV запущено. Интервал: " + interval + " сек.");
        view.displayMessage("Файл: security_logs.csv (на сервере)");
        view.displayMessage("Нажмите Ctrl+C для остановки.");
    }

    private void waitForInterrupt() {
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            shutdown();
        }
    }

    public void shutdown() {
        isMonitoring = false;
        isLogging = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }

        remoteController.close();
        view.displayMessage("\nКлиент завершен.");
    }
}
