package server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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

/**
 * Обработчик клиентских подключений на сервере
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final SecuritySystemController systemController;
    private final Gson gson;
    private String clientAddress;

    public ClientHandler(Socket socket, SecuritySystemController controller) {
        this.clientSocket = socket;
        this.systemController = controller;
        this.gson = new Gson();
        this.clientAddress = socket.getInetAddress().toString();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            log("Клиент подключен");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                try {
                    Request request = gson.fromJson(inputLine, Request.class);
                    log("Получен запрос: " + request.getCommand());

                    Response response = processRequest(request);

                    String jsonResponse = gson.toJson(response);
                    out.println(jsonResponse);

                    log("Ответ отправлен: " + (response.isSuccess() ? "SUCCESS" : "ERROR"));

                } catch (Exception e) {
                    log("Ошибка обработки: " + e.getMessage());
                    e.printStackTrace();
                    Response errorResponse = Response.error("Ошибка обработки запроса: " + e.getMessage());
                    out.println(gson.toJson(errorResponse));
                }
            }

        } catch (IOException e) {
            log("Ошибка: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                log("Клиент отключен");
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
            log("Обработка команды: " + command);

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

                case NetworkConstant.GET_CURRENT_FILE_NAME ->
                        Response.success("Текущий файл", systemController.getCurrentFileName());

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
            log("Ошибка обработки запроса: " + e.getMessage());
            return Response.error("Ошибка сервера: " + e.getMessage());
        }
    }

    private Response handleGetAllSystems() {
        List<SecuritySystem> systems = systemController.getAllSystems();
        return Response.success("Список систем получен", systems);
    }

    private Response handleGetSystem(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        SecuritySystem system = systemController.getSystem(index);

        if (system != null) {
            return Response.success("Система найдена", system);
        } else {
            return Response.error("Система с индексом " + index + " не найдена");
        }
    }

    private Response handleGetSystemById(Request request) {
        String systemId = (String) request.getParam(NetworkConstant.PARAM_SYSTEM_ID);
        if (systemId == null) {
            return Response.error("Отсутствует параметр systemId");
        }

        SecuritySystem system = systemController.getSystemById(systemId);
        if (system != null) {
            return Response.success("Система найдена", system);
        } else {
            return Response.error("Система с ID " + systemId + " не найдена");
        }
    }

    private Response handleAddSystem(Request request) {
        String systemJson = (String) request.getParam(NetworkConstant.PARAM_SYSTEM_JSON);
        String systemType = (String) request.getParam(NetworkConstant.PARAM_SYSTEM_TYPE);

        if (systemJson == null || systemType == null) {
            return Response.error("Отсутствуют необходимые параметры");
        }

        try {
            SecuritySystem system = switch (systemType) {
                case "HomeAlarmSystem" -> gson.fromJson(systemJson, HomeAlarmSystem.class);
                case "BiometricLock" -> gson.fromJson(systemJson, BiometricLock.class);
                case "CarAlarmSystem" -> gson.fromJson(systemJson, CarAlarmSystem.class);
                default -> null;
            };

            if (system != null) {
                systemController.addSystem(system);
                return Response.success("Система добавлена", system);
            } else {
                return Response.error("Неизвестный тип системы: " + systemType);
            }
        } catch (JsonSyntaxException e) {
            return Response.error("Ошибка парсинга системы: " + e.getMessage());
        }
    }

    private Response handleRemoveSystem(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        boolean result = systemController.removeSystem(index);

        if (result) {
            return Response.success("Система удалена");
        } else {
            return Response.error("Не удалось удалить систему с индексом " + index);
        }
    }

    private Response handleRemoveSystemById(Request request) {
        String systemId = (String) request.getParam(NetworkConstant.PARAM_SYSTEM_ID);
        if (systemId == null) {
            return Response.error("Отсутствует параметр systemId");
        }

        boolean result = systemController.removeSystemById(systemId);
        if (result) {
            return Response.success("Система удалена");
        } else {
            return Response.error("Не удалось удалить систему с ID " + systemId);
        }
    }

    private Response handleArmSystem(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        boolean result = systemController.armSystem(index);

        if (result) {
            return Response.success("Система поставлена на охрану");
        } else {
            return Response.error("Не удалось поставить систему на охрану");
        }
    }

    private Response handleDisarmSystem(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        boolean result = systemController.disarmSystem(index);

        if (result) {
            return Response.success("Система снята с охраны");
        } else {
            return Response.error("Не удалось снять систему с охраны");
        }
    }

    private Response handleSetSecurityMode(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        String mode = (String) request.getParam(NetworkConstant.PARAM_MODE);

        if (indexObj == null || mode == null) {
            return Response.error("Отсутствуют необходимые параметры");
        }

        int index = indexObj.intValue();
        boolean result = systemController.setSecurityMode(index, mode);

        if (result) {
            return Response.success("Режим безопасности установлен");
        } else {
            return Response.error("Не удалось установить режим безопасности");
        }
    }

    private Response handlePerformSelfTest(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        boolean result = systemController.performSelfTest(index);

        return Response.success("Самодиагностика выполнена", result);
    }

    private Response handleSimulateEmergency(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        EmergencyEvent event = systemController.simulateEmergency(index);

        if (event != null) {
            return Response.success("Экстренная ситуация сымитирована", event);
        } else {
            return Response.error("Не удалось симулировать экстренную ситуацию");
        }
    }

    private Response handleGetStatusReport(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        SystemStatusReport report = systemController.getStatusReport(index);

        if (report != null) {
            return Response.success("Отчет получен", report);
        } else {
            return Response.error("Не удалось получить отчет");
        }
    }

    private Response handleCalibrateSensors(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        boolean result = systemController.calibrateSensors(index);

        if (result) {
            return Response.success("Сенсоры откалиброваны");
        } else {
            return Response.error("Не удалось откалибровать сенсоры");
        }
    }

    private Response handleCheckConnectivity(Request request) {
        Double indexObj = (Double) request.getParam(NetworkConstant.PARAM_INDEX);
        if (indexObj == null) {
            return Response.error("Отсутствует параметр index");
        }

        int index = indexObj.intValue();
        boolean result = systemController.checkConnectivity(index);

        return Response.success("Проверка подключения выполнена", result);
    }

    private Response handleLoadSystemsFromFile(Request request) {
        String fileName = (String) request.getParam(NetworkConstant.PARAM_FILE_NAME);
        Boolean append = (Boolean) request.getParam(NetworkConstant.PARAM_APPEND);

        if (fileName == null) {
            return Response.error("Отсутствует параметр fileName");
        }

        boolean appendMode = append != null && append;
        boolean result = systemController.loadSystemsFromFile(fileName, appendMode);

        if (result) {
            return Response.success("Системы загружены из файла", systemController.getSystemCount());
        } else {
            return Response.error("Не удалось загрузить системы из файла");
        }
    }

    private Response handleSetFileName(Request request) {
        String fileName = (String) request.getParam(NetworkConstant.PARAM_FILE_NAME);
        if (fileName == null) {
            return Response.error("Отсутствует параметр fileName");
        }

        systemController.setFileName(fileName);
        return Response.success("Имя файла изменено");
    }

    private Response handleLogAllSystemsState() {
        systemController.logAllSystemsState();
        return Response.success("Состояния всех систем залогированы");
    }

    private Response handleGetCsvLogs(Request request) {
        String systemId = (String) request.getParam(NetworkConstant.PARAM_SYSTEM_ID);
        Double limitObj = (Double) request.getParam(NetworkConstant.PARAM_LIMIT);

        if (systemId == null) {
            return Response.error("Отсутствует параметр systemId");
        }

        int limit = limitObj != null ? limitObj.intValue() : 50;
        List<String> logs = systemController.getCsvLogger().getLogsBySystemId(systemId, limit);

        return Response.success("Логи получены", logs);
    }

    private Response handleGetRecentLogs(Request request) {
        Double limitObj = (Double) request.getParam(NetworkConstant.PARAM_LIMIT);
        int limit = limitObj != null ? limitObj.intValue() : 100;

        List<String> logs = systemController.getCsvLogger().getRecentLogs(limit);
        return Response.success("Логи получены", logs);
    }

    private Response handleSetCsvLogInterval(Request request) {
        Double intervalObj = (Double) request.getParam(NetworkConstant.PARAM_INTERVAL);
        if (intervalObj == null) {
            return Response.error("Отсутствует параметр interval");
        }

        int interval = intervalObj.intValue();
        systemController.setCSVLogInterval(interval);
        return Response.success("Интервал логирования установлен");
    }

    private Response handleSaveSystemsToFile(Request request) {
        String fileName = (String) request.getParam(NetworkConstant.PARAM_FILE_NAME);
        if (fileName == null) {
            return Response.error("Отсутствует параметр fileName");
        }

        try {
            systemController.saveSystemsToFile(fileName);
            return Response.success("Системы сохранены в файл");
        } catch (Exception e) {
            return Response.error("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void log(String message) {
        System.out.println("[" + clientAddress + "] " + message);
    }
}
