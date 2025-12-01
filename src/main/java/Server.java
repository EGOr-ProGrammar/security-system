import config.ConfigManager;
import controllers.SecuritySystemController;
import models.CSVLogger;
import models.TextFileParser;
import server.ClientHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Серверное приложение для управления системами безопасности
 */
public class Server {
    private static final String CONFIG_FILE = "application.properties";
    private static int PORT;
    private static String DATA_FILE;

    private final SecuritySystemController systemController;
    private final ExecutorService threadPool;
    private volatile boolean running;

    public Server(SecuritySystemController controller) {
        this.systemController = controller;
        this.threadPool = Executors.newCachedThreadPool();
        this.running = true;
    }

    public void start() {
        System.out.println("=== Security Systems Server ===");
        System.out.println("Загрузка конфигурации из " + CONFIG_FILE);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен на порту: " + PORT);
            System.out.println("Файл данных: " + DATA_FILE);

            // Загрузка систем из файла
            if (systemController.loadSystemsFromFile(DATA_FILE, false)) {
                System.out.println("Загружено систем: " + systemController.getSystemCount());
            } else {
                System.out.println("Предупреждение: не удалось загрузить системы из файла");
            }

            System.out.println("Ожидание подключений клиентов...\n");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Новое подключение: " + clientSocket.getInetAddress());

                    ClientHandler handler = new ClientHandler(clientSocket, systemController);
                    threadPool.execute(handler);

                } catch (IOException e) {
                    if (running) {
                        System.err.println("Ошибка при принятии подключения: " + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        running = false;
        threadPool.shutdown();
        systemController.close();
        System.out.println("\nСервер остановлен.");
    }

    private static void loadConfiguration() {
        Properties props = new Properties();

        // Попытка 1: Загрузить из classpath (внутри JAR)
        try (InputStream is = Server.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                props.load(is);
                PORT = Integer.parseInt(props.getProperty("port", "5000"));
                DATA_FILE = props.getProperty("datafile", "security_systems.txt");
                return;
            }
        } catch (IOException | NumberFormatException e) {
            // Продолжаем к следующей попытке
        }

        // Попытка 2: Загрузить из текущей директории
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            PORT = Integer.parseInt(props.getProperty("port", "5000"));
            DATA_FILE = props.getProperty("datafile", "security_systems.txt");
            return;
        } catch (IOException | NumberFormatException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
        }

        // Значения по умолчанию
        System.out.println("Используются значения по умолчанию");
        PORT = 5000;
        DATA_FILE = "security_systems.txt";
    }

    public static void main(String[] args) {
        loadConfiguration();

        // Инициализация компонентов
        TextFileParser parser = new TextFileParser(DATA_FILE);
        CSVLogger csvLogger = new CSVLogger("security_logs.csv");
        SecuritySystemController controller = new SecuritySystemController(parser, csvLogger, DATA_FILE);

        // Запуск сервера
        Server server = new Server(controller);

        // Обработка завершения (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nПолучен сигнал завершения...");
            server.shutdown();
        }));

        server.start();
    }
}
