package views;

import models.CSVLogger;
import models.SecuritySystem;

import java.util.List;

public class ConsoleView {

    public void displayHelp() {
        System.out.println("""
            Использование: java -jar security-system.jar [ключи]
            Ключи: -h, --help, -f, --file, -s, --state, -c, --continuous, -l, --log
            """);
    }

    public void displaySystemState(List<SecuritySystem> systems, String fileName) {
        System.out.println("\n=== СОСТОЯНИЕ СИСТЕМ ===");
        System.out.println("Файл: " + fileName);
        if(systems.isEmpty()) {
            System.out.println("Нет доступных систем.");
            return;
        }

        for (int i = 0; i < systems.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, systems.get(i));
        }
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayError(String error) {
        System.err.println("Ошибка: " + error);
    }

    public void displayMainMenu(String currentFileName) {
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("1. Добавить систему");
        System.out.println("2. Удалить систему");
        System.out.println("3. Список систем");
        System.out.println("4. Изменить систему");
        System.out.println("5. Операции с системой");
        System.out.println("6. Работа с файлами");
        System.out.println("7. Непрерывный мониторинг");
        System.out.println("8. Логирование в CSV");
        System.out.println("9. Сохранить все журналы событий");
        System.out.println("0. Выход");
        System.out.println("Текущий файл: " + currentFileName);
    }

    public void displaySystemOperationsMenu(SecuritySystem system) {
        System.out.println("\n=== ОПЕРАЦИИ С: " + system.getClass().getSimpleName() + " ===");
        System.out.println("Устройство: " + system);
        System.out.println("1. Установить режим охраны");
        System.out.println("2. Снять режим охраны");
        System.out.println("3. Установить режим безопасности");
        System.out.println("4. Отправить тестовую тревогу");
        System.out.println("5. Самодиагностика");
        System.out.println("6. Получить детальный отчет");
        System.out.println("7. Калибровать сенсоры");
        System.out.println("8. Проверить подключение");
        System.out.println("9. Показать журнал событий");
        System.out.println("10. Специфические функции устройства");
        System.out.println("0. Назад в главное меню");
    }

    public void displayHomeAlarmMenu() {
        System.out.println("\n=== ФУНКЦИИ ДОМАШНЕЙ СИГНАЛИЗАЦИИ ===");
        System.out.println("1. Переключить датчики дверей");
        System.out.println("2. Переключить датчики окон");
        System.out.println("3. Установить чувствительность");
        System.out.println("4. Переключить тихий режим");
        System.out.println("5. Симитировать вторжение");
    }

    public void displayBiometricLockMenu() {
        System.out.println("\n=== ФУНКЦИИ БИОМЕТРИЧЕСКОГО ЗАМКА ===");
        System.out.println("1. Аутентификация пользователя");
        System.out.println("2. Добавить пользователя");
        System.out.println("3. Заблокировать дверь");
        System.out.println("4. Открыть дверь");
        System.out.println("5. Переключить сканер отпечатков");
    }

    public void displayCarAlarmMenu() {
        System.out.println("\n=== ФУНКЦИИ АВТОМОБИЛЬНОЙ СИГНАЛИЗАЦИИ ===");
        System.out.println("1. Активировать режим паники");
        System.out.println("2. Переключить датчик удара");
        System.out.println("3. Переключить датчик наклона");
        System.out.println("4. Установить громкость сигнала");
        System.out.println("5. Симитировать удар");
    }

    public void displayFileOperationsMenu() {
        System.out.println("\n=== ОПЕРАЦИИ С ФАЙЛАМИ ===");
        System.out.println("1. Загрузить из файла");
        System.out.println("2. Изменить файл");
    }

    // Метод для отображения логов через CSVLogger
    public void displayEventLog(CSVLogger csvLogger, String systemId) {
        System.out.println("\n=== ЖУРНАЛ СОБЫТИЙ СИСТЕМЫ: " + systemId + " ===");
        List<String> logs = csvLogger.getLogsBySystemId(systemId, 50);

        if (logs.isEmpty()) {
            System.out.println("Журнал событий пуст");
            return;
        }

        for (String log : logs) {
            String[] parts = log.split(",");
            if (parts.length >= 9) {
                System.out.printf("[%s] %s - %s\n", parts[0], parts[7], parts[8]);
            }
        }
    }

    // Перегрузка для совместимости со старым кодом (временно)
    public void displayEventLog(List<String> eventLog) {
        if (eventLog.isEmpty()) {
            displayMessage("Журнал событий пуст");
        } else {
            displayMessage("=== ЖУРНАЛ СОБЫТИЙ ===");
            eventLog.forEach(System.out::println);
        }
    }

    public void displayAllLogs(CSVLogger csvLogger) {
        System.out.println("\n=== ВСЕ ЛОГИ СИСТЕМЫ ===");
        List<String> logs = csvLogger.getRecentLogs(100);

        if (logs.isEmpty()) {
            System.out.println("Логи отсутствуют.");
            return;
        }

        for (String log : logs) {
            String[] parts = log.split(",");
            if (parts.length >= 9) {
                System.out.printf("[%s] %s - %s - %s\n", parts[0], parts[1], parts[7], parts[8]);
            }
        }
    }

     public void waitForEnter() {
         ConsoleInputHandler.waitForEnter();
     }

    public void displayPrompt(String prompt) {
        System.out.print(prompt + ": ");
    }
}
