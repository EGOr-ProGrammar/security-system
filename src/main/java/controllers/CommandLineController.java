package controllers;

import views.ConsoleView;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Управляет считыванием и исполнением флагов из аргументов командной строки.
 * -h/--help: справка
 * -f/--file: загрузка из файла
 * -s/--state: вывод состояния систем
 * -c/--continuous: непрерывный мониторинг с периодом
 * -l/--log: логирование в CSV с периодом
 */
public class CommandLineController {
    private final SecuritySystemController systemController;
    private final ConsoleView view;
    private final ScheduledExecutorService scheduler;
    private boolean isMonitoring = false;
    private boolean isLogging = false;

    public CommandLineController(SecuritySystemController systemController, ConsoleView view) {
        this.systemController = systemController;
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
                            if (systemController.loadSystemsFromFile(fileName, false)) {
                                view.displayMessage("Системы успешно загружены из файла: " + fileName);
                            } else {
                                view.displayError("Ошибка загрузки файла: " + fileName);
                            }
                        } else {
                            view.displayError("Ключ -f требует имя файла");
                            return true;
                        }
                        break;

                    case "-s":
                    case "--state":
                        // Разовый вывод информации о состоянии объектов
                        systemController.loadSystemsFromFile(systemController.getCurrentFileName(), false);
                        view.displaySystemState(
                                systemController.getAllSystems(),
                                systemController.getCurrentFileName()
                        );

                        return true;

                    case "-c":
                    case "--continuous":
                        // Циклический вывод информации о состоянии объектов с настройкой периода
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
                            systemController.setFileName(newFile);
                            view.displayMessage("Текущий файл изменен на: " + newFile);
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

            systemController.loadSystemsFromFile(systemController.getCurrentFileName(), false);
            view.displaySystemState(
                    systemController.getAllSystems(),
                    systemController.getCurrentFileName()
            );
            return true;

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
        systemController.loadSystemsFromFile(systemController.getCurrentFileName(), false);
        isMonitoring = true;

        scheduler.scheduleAtFixedRate(() -> {
            if (isMonitoring) {
                System.out.println("\n========================================");
                System.out.println("Время: " + java.time.LocalDateTime.now());
                view.displaySystemState(
                        systemController.getAllSystems(),
                        systemController.getCurrentFileName()
                );
                System.out.println("========================================");
            }
        }, 0, interval, TimeUnit.SECONDS);

        view.displayMessage("Непрерывный мониторинг запущен. Интервал: " + interval + " сек.");
        view.displayMessage("Нажмите Ctrl+C для остановки.");
    }

    private void startCSVLogging(int interval) {
        systemController.loadSystemsFromFile(systemController.getCurrentFileName(), false);
        isLogging = true;
        systemController.setCSVLogInterval(interval);

        scheduler.scheduleAtFixedRate(() -> {
            if (isLogging) {
                systemController.logAllSystemsState();
                System.out.println("[" + java.time.LocalDateTime.now() + "] Записано " +
                        systemController.getSystemCount() + " систем в CSV");
            }
        }, 0, interval, TimeUnit.SECONDS);

        view.displayMessage("Логирование в CSV запущено. Интервал: " + interval + " сек.");
        view.displayMessage("Файл: security_logs.csv");
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
        systemController.close();
        view.displayMessage("\nПрограмма завершена.");
    }
}
