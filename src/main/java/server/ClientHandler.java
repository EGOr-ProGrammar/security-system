package server;

import com.google.gson.Gson;
import controllers.SecuritySystemController;
import models.*;
import models.dto.EmergencyEvent;
import models.dto.SystemStatusReport;
import network.NetworkConstant;
import network.Request;
import network.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SecuritySystemController systemController;
    private final CSVLogger csvLogger;
    private final Gson gson;
    private String clientAddress;

    public ClientHandler(Socket socket, SecuritySystemController controller, CSVLogger csvLogger) {
        this.clientSocket = socket;
        this.systemController = controller;
        this.csvLogger = csvLogger;
        this.gson = new Gson();
        this.clientAddress = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            log("Клиент подключен");
            csvLogger.logSystemEvent(EventType.CLIENT_CONNECTED, "Адрес: " + clientAddress);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    Request request = gson.fromJson(inputLine, Request.class);
                    String command = request.getCommand();

                    log("Получен запрос: " + command);
                    csvLogger.logSystemEvent(EventType.COMMAND_RECEIVED,
                            "Команда: " + command + " от " + clientAddress);

                    Response response = processRequest(request);

                    String jsonResponse = gson.toJson(response);
                    out.println(jsonResponse);

                    if (response.isSuccess()) {
                        log("Команда выполнена успешно: " + command);
                        csvLogger.logSystemEvent(EventType.COMMAND_EXECUTED,
                                "Команда: " + command + " от " + clientAddress);
                    } else {
                        log("Ошибка выполнения команды: " + command);
                        csvLogger.logSystemEvent(EventType.COMMAND_FAILED,
                                "Команда: " + command + ", Ошибка: " + response.getMessage());
                    }

                } catch (Exception e) {
                    log("Ошибка обработки: " + e.getMessage());
                    csvLogger.logSystemEvent(EventType.COMMAND_FAILED,
                            "Ошибка обработки от " + clientAddress + ": " + e.getMessage());

                    Response errorResponse = Response.error("Ошибка обработки запроса: " + e.getMessage());
                    out.println(gson.toJson(errorResponse));
                }
            }

        } catch (IOException e) {
            log("Ошибка соединения: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                log("Клиент отключен");
                csvLogger.logSystemEvent(EventType.CLIENT_DISCONNECTED, "Адрес: " + clientAddress);
            } catch (IOException e) {
                log("Ошибка закрытия сокета: " + e.getMessage());
            }
        }
    }

    private Response processRequest(Request request) {
        try {
            if (request == null || request.getCommand() == null) {
                return Response.error("Пустой запрос");
            }

            String command = request.getCommand();

            return switch (command) {
                case NetworkConstant.PING ->
                        Response.success("PONG");

                case NetworkConstant.GET_ALL_SYSTEMS ->
                        handleGetAllSystems();

                case NetworkConstant.GET_SYSTEM ->
                        handleGetSystem(request);

                case NetworkConstant.GET_SYSTEM_BY_ID ->
                        handleGetSystemById(request);

                case NetworkConstant.ADD_SYSTEM ->
                        handleAddSystem(request);

                case NetworkConstant.REMOVE_SYSTEM ->
                        handleRemoveSystem(request);

                case NetworkConstant.REMOVE_SYSTEM_BY_ID ->
                        handleRemoveSystemById(request);

                case NetworkConstant.ARM_SYSTEM ->
                        handleArmSystem(request);

                case NetworkConstant.DISARM_SYSTEM ->
                        handleDisarmSystem(request);

                case NetworkConstant.SET_SECURITY_MODE ->
                        handleSetSecurityMode(request);

                case NetworkConstant.PERFORM_SELF_TEST ->
                        handlePerformSelfTest(request);

                case NetworkConstant.SIMULATE_EMERGENCY ->
                        handleSimulateEmergency(request);

                case NetworkConstant.GET_STATUS_REPORT ->
                        handleGetStatusReport(request);

                case NetworkConstant.CALIBRATE_SENSORS ->
                        handleCalibrateSensors(request);

                case NetworkConstant.CHECK_CONNECTIVITY ->
                        handleCheckConnectivity(request);

                case NetworkConstant.LOAD_SYSTEMS_FROM_FILE ->
                        handleLoadSystemsFromFile(request);

                case NetworkConstant.SET_FILE_NAME ->
                        handleSetFileName(request);

                case NetworkConstant.GET_CURRENT_FILE_NAME -> {
                    String fileName = systemController.getCurrentFileName();
                    yield Response.success("Текущий файл", fileName != null ? fileName : "");
                }

                case NetworkConstant.GET_SYSTEM_COUNT ->
                        Response.success("Количество систем", systemController.getSystemCount());

                case NetworkConstant.LOG_ALL_SYSTEMS_STATE ->
                        handleLogAllSystemsState();

                case NetworkConstant.GET_CSV_LOGS ->
                        handleGetCsvLogs(request);

                case NetworkConstant.GET_RECENT_LOGS ->
                        handleGetRecentLogs(request);

                case NetworkConstant.SET_CSV_LOG_INTERVAL ->
                        handleSetCsvLogInterval(request);

                case NetworkConstant.SAVE_SYSTEMS_TO_FILE ->
                        handleSaveSystemsToFile(request);

                default ->
                        Response.error("Неизвестная команда: " + command);
            };

        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("Ошибка обработки запроса: " + e.getMessage());
        }
    }

    // ============= ОБРАБОТЧИКИ КОМАНД =============

    private Response handleGetAllSystems() {
        List<SecuritySystem> systems = systemController.getAllSystems();
        return Response.success("Список систем", systems);
    }

    private Response handleGetSystem(Request request) {
        Object indexObj = request.getParam("index");
        if (indexObj == null) {
            return Response.error("Не указан индекс системы");
        }
        int index = ((Number) indexObj).intValue();
        SecuritySystem system = systemController.getSystem(index);
        if (system != null) {
            return Response.success("Система получена", system);
        } else {
            return Response.error("Система не найдена");
        }
    }

    private Response handleGetSystemById(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            return Response.success("Система получена", system);
        } else {
            return Response.error("Система не найдена");
        }
    }

    private Response handleAddSystem(Request request) {
        try {
            String systemData = (String) request.getParam("systemData");
            if (systemData == null) {
                return Response.error("Не указаны данные системы");
            }

            SecuritySystem system = SecuritySystemStringParser.parse(systemData);

            if (system != null) {
                systemController.addSystem(system);
                csvLogger.logSystemEvent(EventType.SYSTEM_ADDED,
                        "ID: " + system.getSystemId() + ", Тип: " + system.getClass().getSimpleName() +
                                ", Клиент: " + clientAddress);
                return Response.success("Система добавлена");
            } else {
                return Response.error("Не удалось создать систему из данных");
            }
        } catch (Exception e) {
            return Response.error("Ошибка добавления системы: " + e.getMessage());
        }
    }

    private Response handleRemoveSystem(Request request) {
        Object indexObj = request.getParam("index");
        if (indexObj == null) {
            return Response.error("Не указан индекс системы");
        }
        int index = ((Number) indexObj).intValue();

        if (systemController.removeSystem(index)) {
            csvLogger.logSystemEvent(EventType.SYSTEM_REMOVED,
                    "Индекс: " + index + ", Клиент: " + clientAddress);
            return Response.success("Система удалена");
        } else {
            return Response.error("Не удалось удалить систему");
        }
    }

    private Response handleRemoveSystemById(Request request) {
        try {
            String systemId = (String) request.getParam("systemId");
            if (systemId == null) {
                return Response.error("Не указан ID системы");
            }

            SecuritySystem system = systemController.getSystemById(systemId);

            if (system != null) {
                if (systemController.removeSystemById(systemId)) {
                    csvLogger.logSystemEvent(EventType.SYSTEM_REMOVED,
                            "ID: " + systemId + ", Клиент: " + clientAddress);
                    return Response.success("Система удалена");
                }
            }
            return Response.error("Система не найдена");
        } catch (Exception e) {
            return Response.error("Ошибка удаления системы: " + e.getMessage());
        }
    }

    private Response handleArmSystem(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            system.armSystem();
            return Response.success("Система поставлена на охрану");
        }
        return Response.error("Система не найдена");
    }

    private Response handleDisarmSystem(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            system.disarmSystem();
            return Response.success("Система снята с охраны");
        }
        return Response.error("Система не найдена");
    }

    private Response handleSetSecurityMode(Request request) {
        String systemId = (String) request.getParam("systemId");
        String mode = (String) request.getParam("mode");

        if (systemId == null || mode == null) {
            return Response.error("Не указаны обязательные параметры");
        }

        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            try {
                system.setSecurityMode(mode);
                return Response.success("Режим изменен");
            } catch (IllegalArgumentException e) {
                return Response.error(e.getMessage());
            }
        }
        return Response.error("Система не найдена");
    }

    private Response handlePerformSelfTest(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            boolean result = system.performSelfTest();
            return Response.success("Самодиагностика завершена", result);
        }
        return Response.error("Система не найдена");
    }

    private Response handleSimulateEmergency(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            EmergencyEvent event = system.simulateEmergency();
            return Response.success("Событие сгенерировано", event);
        }
        return Response.error("Система не найдена");
    }

    private Response handleGetStatusReport(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            SystemStatusReport report = system.getStatusReport();
            return Response.success("Отчет получен", report);
        }
        return Response.error("Система не найдена");
    }

    private Response handleCalibrateSensors(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            system.calibrateSensors();
            return Response.success("Датчики откалиброваны");
        }
        return Response.error("Система не найдена");
    }

    private Response handleCheckConnectivity(Request request) {
        String systemId = (String) request.getParam("systemId");
        if (systemId == null) {
            return Response.error("Не указан ID системы");
        }
        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            boolean connected = system.checkConnectivity();
            return Response.success("Проверка связи выполнена", connected);
        }
        return Response.error("Система не найдена");
    }

    private Response handleLoadSystemsFromFile(Request request) {
        try {
            String fileName = (String) request.getParam("fileName");
            Object replaceObj = request.getParam("replace");
            boolean replace = replaceObj != null && (Boolean) replaceObj;

            if (fileName == null) {
                return Response.error("Не указано имя файла");
            }

            if (systemController.loadSystemsFromFile(fileName, replace)) {
                int count = systemController.getSystemCount();
                csvLogger.logSystemEvent(EventType.FILE_LOADED,
                        "Файл: " + fileName + ", Загружено систем: " + count +
                                ", Клиент: " + clientAddress);
                return Response.success("Загружено систем: " + count, count);
            } else {
                return Response.error("Не удалось загрузить системы из файла");
            }
        } catch (Exception e) {
            return Response.error("Ошибка загрузки: " + e.getMessage());
        }
    }

    private Response handleSaveSystemsToFile(Request request) {
        try {
            String fileName = (String) request.getParam("fileName");
            if (fileName == null) {
                return Response.error("Не указано имя файла");
            }

            if (systemController.saveSystemsToFile(fileName)) {
                csvLogger.logSystemEvent(EventType.FILE_SAVED,
                        "Файл: " + fileName + ", Сохранено систем: " + systemController.getSystemCount() +
                                ", Клиент: " + clientAddress);
                return Response.success("Системы сохранены в файл");
            } else {
                return Response.error("Не удалось сохранить системы");
            }
        } catch (Exception e) {
            return Response.error("Ошибка сохранения: " + e.getMessage());
        }
    }

    private Response handleSetFileName(Request request) {
        String fileName = (String) request.getParam("fileName");
        if (fileName == null) {
            return Response.error("Не указано имя файла");
        }
        systemController.setCurrentFileName(fileName);
        return Response.success("Имя файла установлено");
    }

    private Response handleLogAllSystemsState() {
        systemController.logAllSystemsState();
        return Response.success("Состояние систем записано в лог");
    }

    private Response handleGetCsvLogs(Request request) {
        String systemId = (String) request.getParam("systemId");
        Object countObj = request.getParam("count");

        if (systemId == null || countObj == null) {
            return Response.error("Не указаны обязательные параметры");
        }

        int count = ((Number) countObj).intValue();
        List<String> logs = systemController.getCsvLogger().getLogsBySystemId(systemId, count);
        return Response.success("Логи получены", logs);
    }

    private Response handleGetRecentLogs(Request request) {
        Object countObj = request.getParam("count");
        if (countObj == null) {
            return Response.error("Не указано количество записей");
        }

        int count = ((Number) countObj).intValue();
        List<String> logs = systemController.getCsvLogger().getRecentLogs(count);
        return Response.success("Логи получены", logs);
    }

    private Response handleSetCsvLogInterval(Request request) {
        Object intervalObj = request.getParam("interval");
        if (intervalObj == null) {
            return Response.error("Не указан интервал");
        }

        int interval = ((Number) intervalObj).intValue();
        systemController.getCsvLogger().setLogInterval(interval);
        return Response.success("Интервал установлен");
    }

    private void log(String message) {
        System.out.println("[" + clientAddress + "] " + message);
    }
}
