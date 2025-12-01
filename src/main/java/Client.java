import client.NetworkClient;
import client.RemoteInteractiveModeController;
import client.RemoteSecuritySystemController;
import controllers.ClientCommandLineController;
import views.ConsoleInputHandler;
import views.ConsoleView;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Главный класс клиентского приложения
 */
public class Client {
    private static final String CONFIG_FILE = "application.properties";
    private static String HOST;
    private static int PORT;
    private static volatile boolean isShuttingDown = false;

    public static void main(String[] args) {
        loadConfiguration();

        System.out.println("=== Security Systems Client ===");
        System.out.println("Подключение к серверу " + HOST + ":" + PORT);

        // Создаем сетевой клиент
        NetworkClient networkClient = new NetworkClient(HOST, PORT);

        // Пытаемся подключиться
        if (!networkClient.connect()) {
            System.err.println("Не удалось подключиться к серверу. Проверьте, что сервер запущен.");
            System.exit(1);
        }

        // Создаем удаленный контроллер
        RemoteSecuritySystemController remoteController = new RemoteSecuritySystemController(networkClient);
        ConsoleView view = new ConsoleView();

        // Обработка завершения (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!isShuttingDown) {
                isShuttingDown = true;
                System.out.println("\n\nЗавершение работы клиента...");
                ConsoleInputHandler.shutdown();
                try {
                    remoteController.close();
                } catch (Exception e) {
                    // Игнорируем ошибки при закрытии
                }
            }
        }));

        // Обработка аргументов командной строки
        ClientCommandLineController cmdController = new ClientCommandLineController(remoteController, view);

        if (args.length > 0) {
            boolean shouldExit = cmdController.processArgs(args);
            if (shouldExit) {
                isShuttingDown = true;
                remoteController.close();
                return;
            }
        }

        // Запуск интерактивного режима
        try {
            RemoteInteractiveModeController interactiveController =
                    new RemoteInteractiveModeController(remoteController, view);
            interactiveController.run();
        } catch (Exception e) {
            if (!isShuttingDown) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        } finally {
            if (!isShuttingDown) {
                isShuttingDown = true;
                remoteController.close();
            }
        }
    }

    private static void loadConfiguration() {
        Properties props = new Properties();

        // Попытка загрузить из classpath (внутри JAR)
        try (InputStream is = Client.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                HOST = props.getProperty("ip", "127.0.0.1");
                PORT = Integer.parseInt(props.getProperty("port", "5000"));
                System.out.println("Конфигурация загружена из classpath");
                return;
            }
        } catch (IOException | NumberFormatException e) {

        }

        // Попытка загрузить из текущей директории
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            HOST = props.getProperty("ip", "127.0.0.1");
            PORT = Integer.parseInt(props.getProperty("port", "5000"));
            System.out.println("Конфигурация загружена из файла: " + CONFIG_FILE);
            return;
        } catch (IOException | NumberFormatException e) {

        }

        // Использовать значения по умолчанию
        System.out.println("Ошибка загрузки конфигурации: application.properties (Не удается найти указанный файл)");
        System.out.println("Используются значения по умолчанию");
        HOST = "127.0.0.1";
        PORT = 5000;
    }
}
