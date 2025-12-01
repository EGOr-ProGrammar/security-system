package client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import models.*;
import models.dto.EmergencyEvent;
import models.dto.SystemStatusReport;
import network.NetworkConstant;
import network.Request;
import network.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Прокси-контроллер для работы с удаленным сервером
 * Реализует тот же интерфейс, что и SecuritySystemController,
 * но вместо локальной работы отправляет запросы на сервер
 */
public class RemoteSecuritySystemController {
    private final NetworkClient networkClient;
    private final Gson gson;
    private String currentFileName;

    public RemoteSecuritySystemController(NetworkClient networkClient) {
        this.networkClient = networkClient;
        this.gson = networkClient.getGson();
        this.currentFileName = "";
    }

    public void addSystem(SecuritySystem system) throws IOException {
        Request request = new Request(NetworkConstant.ADD_SYSTEM);
        request.addParam(NetworkConstant.PARAM_SYSTEM_JSON, gson.toJson(system));
        request.addParam(NetworkConstant.PARAM_SYSTEM_TYPE, system.getClass().getSimpleName());

        Response response = networkClient.sendRequest(request);
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    public boolean removeSystem(int index) throws IOException {
        Request request = new Request(NetworkConstant.REMOVE_SYSTEM);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        return response.isSuccess();
    }

    public boolean removeSystemById(String id) throws IOException {
        Request request = new Request(NetworkConstant.REMOVE_SYSTEM_BY_ID);
        request.addParam(NetworkConstant.PARAM_SYSTEM_ID, id);

        Response response = networkClient.sendRequest(request);
        return response.isSuccess();
    }

    public SecuritySystem getSystem(int index) throws IOException {
        Request request = new Request(NetworkConstant.GET_SYSTEM);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            return parseSecuritySystem(response.getData());
        }
        return null;
    }

    public SecuritySystem getSystemById(String id) throws IOException {
        Request request = new Request(NetworkConstant.GET_SYSTEM_BY_ID);
        request.addParam(NetworkConstant.PARAM_SYSTEM_ID, id);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            return parseSecuritySystem(response.getData());
        }
        return null;
    }

    public List<SecuritySystem> getAllSystems() throws IOException {
        Request request = new Request(NetworkConstant.GET_ALL_SYSTEMS);
        Response response = networkClient.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            List<SecuritySystem> systems = new ArrayList<>();

            @SuppressWarnings("unchecked")
            List<Object> dataList = (List<Object>) response.getData();

            for (Object obj : dataList) {
                SecuritySystem system = parseSecuritySystem(obj);
                if (system != null) {
                    systems.add(system);
                }
            }
            return systems;
        }
        return new ArrayList<>();
    }

    public boolean loadSystemsFromFile(String fileName, boolean append) throws IOException {
        Request request = new Request(NetworkConstant.LOAD_SYSTEMS_FROM_FILE);
        request.addParam(NetworkConstant.PARAM_FILE_NAME, fileName);
        request.addParam(NetworkConstant.PARAM_APPEND, append);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess()) {
            this.currentFileName = fileName;
        }
        return response.isSuccess();
    }

    public void setFileName(String fileName) throws IOException {
        Request request = new Request(NetworkConstant.SET_FILE_NAME);
        request.addParam(NetworkConstant.PARAM_FILE_NAME, fileName);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess()) {
            this.currentFileName = fileName;
        }
    }

    public String getCurrentFileName() throws IOException {
        try {
            Request request = new Request(NetworkConstant.GET_CURRENT_FILE_NAME);
            Response response = networkClient.sendRequest(request);

            if (response.isSuccess()) {
                if (response.getData() != null) {
                    this.currentFileName = response.getData().toString();
                } else {
                    this.currentFileName = "";
                }
                return this.currentFileName;
            } else {
                System.err.println("Ошибка получения имени файла: " + response.getMessage());
                return this.currentFileName != null ? this.currentFileName : "";
            }
        } catch (IOException e) {
            System.err.println("Ошибка связи при получении имени файла: " + e.getMessage());
            throw e;
        }
    }


    public boolean armSystem(int index) throws IOException {
        Request request = new Request(NetworkConstant.ARM_SYSTEM);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        return response.isSuccess();
    }

    public boolean disarmSystem(int index) throws IOException {
        Request request = new Request(NetworkConstant.DISARM_SYSTEM);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        return response.isSuccess();
    }

    public boolean setSecurityMode(int index, String mode) throws IOException {
        Request request = new Request(NetworkConstant.SET_SECURITY_MODE);
        request.addParam(NetworkConstant.PARAM_INDEX, index);
        request.addParam(NetworkConstant.PARAM_MODE, mode);

        Response response = networkClient.sendRequest(request);
        return response.isSuccess();
    }

    public boolean performSelfTest(int index) throws IOException {
        Request request = new Request(NetworkConstant.PERFORM_SELF_TEST);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            return (Boolean) response.getData();
        }
        return false;
    }

    public EmergencyEvent simulateEmergency(int index) throws IOException {
        Request request = new Request(NetworkConstant.SIMULATE_EMERGENCY);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            String json = gson.toJson(response.getData());
            return gson.fromJson(json, EmergencyEvent.class);
        }
        return null;
    }

    public SystemStatusReport getStatusReport(int index) throws IOException {
        Request request = new Request(NetworkConstant.GET_STATUS_REPORT);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            return parseStatusReport(response.getData());
        }
        return null;
    }

    public boolean calibrateSensors(int index) throws IOException {
        Request request = new Request(NetworkConstant.CALIBRATE_SENSORS);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        return response.isSuccess();
    }

    public boolean checkConnectivity(int index) throws IOException {
        Request request = new Request(NetworkConstant.CHECK_CONNECTIVITY);
        request.addParam(NetworkConstant.PARAM_INDEX, index);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            return (Boolean) response.getData();
        }
        return false;
    }

    public void logAllSystemsState() throws IOException {
        Request request = new Request(NetworkConstant.LOG_ALL_SYSTEMS_STATE);
        networkClient.sendRequest(request);
    }

    public void setCSVLogInterval(int seconds) throws IOException {
        Request request = new Request(NetworkConstant.SET_CSV_LOG_INTERVAL);
        request.addParam(NetworkConstant.PARAM_INTERVAL, seconds);
        networkClient.sendRequest(request);
    }

    public int getSystemCount() throws IOException {
        Request request = new Request(NetworkConstant.GET_SYSTEM_COUNT);
        Response response = networkClient.sendRequest(request);

        if (response.isSuccess() && response.getData() != null) {
            return ((Double) response.getData()).intValue();
        }
        return 0;
    }

    public List<String> getCsvLogs(String systemId, int limit) throws IOException {
        Request request = new Request(NetworkConstant.GET_CSV_LOGS);
        request.addParam(NetworkConstant.PARAM_SYSTEM_ID, systemId);
        request.addParam(NetworkConstant.PARAM_LIMIT, limit);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            String json = gson.toJson(response.getData());
            return gson.fromJson(json, listType);
        }
        return new ArrayList<>();
    }

    public List<String> getRecentLogs(int limit) throws IOException {
        Request request = new Request(NetworkConstant.GET_RECENT_LOGS);
        request.addParam(NetworkConstant.PARAM_LIMIT, limit);

        Response response = networkClient.sendRequest(request);
        if (response.isSuccess() && response.getData() != null) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            String json = gson.toJson(response.getData());
            return gson.fromJson(json, listType);
        }
        return new ArrayList<>();
    }

    public void saveSystemsToFile(String filename) throws IOException {
        Request request = new Request(NetworkConstant.SAVE_SYSTEMS_TO_FILE);
        request.addParam(NetworkConstant.PARAM_FILE_NAME, filename);

        Response response = networkClient.sendRequest(request);
        if (!response.isSuccess()) {
            throw new IOException(response.getMessage());
        }
    }

    public boolean hasSystem(int index) throws IOException {
        return index >= 0 && index < getSystemCount();
    }

    public void close() {
        networkClient.disconnect();
    }

    // Вспомогательные методы для парсинга
    private SecuritySystem parseSecuritySystem(Object data) {
        try {
            String json = gson.toJson(data);

            // Определяем тип системы по JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(json, Map.class);

            // Проверяем специфичные поля для определения типа
            if (map.containsKey("doorSensorsActive") || map.containsKey("windowSensorsActive")) {
                return gson.fromJson(json, HomeAlarmSystem.class);
            } else if (map.containsKey("failedAttempts") || map.containsKey("fingerprintEnabled")) {
                return gson.fromJson(json, BiometricLock.class);
            } else if (map.containsKey("shockSensorActive") || map.containsKey("panicModeDuration")) {
                return gson.fromJson(json, CarAlarmSystem.class);
            }

            // По умолчанию пробуем как SecuritySystem
            return gson.fromJson(json, SecuritySystem.class);

        } catch (Exception e) {
            System.err.println("Ошибка парсинга системы: " + e.getMessage());
            return null;
        }
    }

    private SystemStatusReport parseStatusReport(Object data) {
        try {
            String json = gson.toJson(data);

            // Определяем тип отчета
            @SuppressWarnings("unchecked")
            Map<String, Object> map = gson.fromJson(json, Map.class);

            if (map.containsKey("doorSensorsActive")) {
                return gson.fromJson(json, models.dto.HomeAlarmStatusReport.class);
            } else if (map.containsKey("failedAttempts")) {
                return gson.fromJson(json, models.dto.BiometricLockStatusReport.class);
            } else if (map.containsKey("shockSensorActive")) {
                return gson.fromJson(json, models.dto.CarAlarmStatusReport.class);
            }

            return gson.fromJson(json, SystemStatusReport.class);

        } catch (Exception e) {
            System.err.println("Ошибка парсинга отчета: " + e.getMessage());
            return null;
        }
    }
}
