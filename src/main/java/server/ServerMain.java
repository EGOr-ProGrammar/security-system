package server;

import java.net.*;
import java.io.*;
import java.util.Properties;
import controllers.SecuritySystemController;
import models.SecuritySystem;
import models.SecuritySystemStringParser;
import models.TextFileParser;
import models.CSVLogger;
import views.ConsoleView;

public class ServerMain {
    private static int PORT;
    private static String dataFilename;
    private static SecuritySystemController controller;
    private static ConsoleView view;
    private static TextFileParser parser;
    private static CSVLogger logger;

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream in = ServerMain.class.getClassLoader().getResourceAsStream("application.properties")) {
            props.load(in);
        }
        PORT = Integer.parseInt(props.getProperty("port", "5000"));
        dataFilename = props.getProperty("datafile", "securitysystems.txt");
        parser = new TextFileParser();
        logger = new CSVLogger();
        view = new ConsoleView();
        controller = new SecuritySystemController(parser, logger, dataFilename);
        controller.loadSystemsFromFile(dataFilename, true);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started at port " + PORT);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        }
    }

    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String cmd;
            while ((cmd = in.readLine()) != null) {
                String command = cmd.trim();
                String upperCmd = command.toUpperCase();

                if (upperCmd.startsWith("LIST")) {
                    out.println(controller.getAllAsString());
                } else if (upperCmd.startsWith("GET")) {
                    String[] parts = command.split("=", 2);
                    String id = (parts.length == 2) ? parts[1].trim() : null;
                    SecuritySystem obj = (id == null) ? null : controller.getSystemById(id);
                    out.println(obj == null ? "NOT FOUND" : obj.toString());
                } else if (upperCmd.startsWith("ADD")) {
                    String data = command.substring(4).trim();
                    SecuritySystem system = SecuritySystemStringParser.parse(data);
                    if (system != null) {
                        controller.addSystem(system);
                        controller.saveSystemsToFile(dataFilename);
                        out.println("ADDED");
                    } else {
                        out.println("ERROR: parsing failed");
                    }
                } else if (upperCmd.startsWith("REMOVE")) {
                    String[] parts = command.split("=", 2);
                    String id = (parts.length == 2) ? parts[1].trim() : null;
                    boolean ok = (id != null) && controller.removeSystemById(id);
                    if (ok) controller.saveSystemsToFile(dataFilename);
                    out.println(ok ? "REMOVED" : "NOT FOUND");
                } else if (upperCmd.equals("EXIT")) {
                    out.println("GOODBYE");
                    break;
                } else {
                    out.println("ERROR: unknown command");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
