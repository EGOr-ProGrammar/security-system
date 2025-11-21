package controllers;

import models.*;
import models.CSVLogger;
import models.dto.EmergencyEvent;
import models.dto.SystemStatusReport;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SecuritySystemController {

    private final List<SecuritySystem> systems;
    private final TextFileParser textFileParser;
    private final CSVLogger csvLogger;
    private String currentFileName;

    public SecuritySystemController(
            TextFileParser textFileParser,
            CSVLogger csvLogger,
            String initialFileName
    ) {
        this.systems = new ArrayList<>();
        this.textFileParser = textFileParser;
        this.csvLogger = csvLogger;
        this.currentFileName = initialFileName;
    }

    public void addSystem(SecuritySystem system) {
        system.setCsvLogger(csvLogger);
        systems.add(system);
        csvLogger.logEvent(system, EventType.SYSTEM_ADDED);
    }

    public boolean removeSystem(int index) {
        if (index >= 0 && index < systems.size()) {
            SecuritySystem removed = systems.remove(index);
            csvLogger.logEvent(removed, EventType.SYSTEM_REMOVED);
            return true;
        }
        return false;
    }

    public SecuritySystem getSystem(int index) {
        return (index >= 0 && index < systems.size()) ? systems.get(index) : null;
    }

    public List<SecuritySystem> getAllSystems() {
        return new ArrayList<>(systems);
    }

    public boolean loadSystemsFromFile(String fileName, boolean append) {
        try {
            if (!append) {
                systems.clear();
                csvLogger.logSystemEvent(EventType.INFO, "Очистка существующих систем");
            }

            List<SecuritySystem> loadedSystems = textFileParser.readFromFile(fileName);
            for (SecuritySystem system : loadedSystems) {
                system.setCsvLogger(csvLogger);
                systems.add(system);
                csvLogger.logEvent(system, EventType.SYSTEM_LOADED, "Из файла: " + fileName);
            }

            this.currentFileName = fileName;
            csvLogger.logSystemEvent(EventType.INFO, "Загружено " + loadedSystems.size() + " систем из " + fileName);
            return true;
        } catch (Exception e) {
            csvLogger.logSystemEvent(EventType.ERROR, "Ошибка загрузки из " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public void setFileName(String fileName) {
        String oldFile = this.currentFileName;
        this.currentFileName = fileName;
        csvLogger.logSystemEvent(EventType.CONFIG_CHANGED, "Файл изменен с " + oldFile + " на " + fileName);
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public boolean armSystem(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.armSystem();
            return true;
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Постановка на охрану не удалась: неверный индекс " + index);
        return false;
    }

    public boolean disarmSystem(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.disarmSystem();
            return true;
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Снятие с охраны не удалось: неверный индекс " + index);
        return false;
    }

    public boolean setSecurityMode(int index, String mode) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            try {
                system.setSecurityMode(mode);
                return true;
            } catch (IllegalArgumentException e) {
                csvLogger.logEvent(system, EventType.ERROR, "Недопустимый режим: " + mode);
                return false;
            }
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Изменение режима не удалось: неверный индекс " + index);
        return false;
    }

    public boolean performSelfTest(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.performSelfTest();
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Самодиагностика не удалась: неверный индекс " + index);
        return false;
    }

    public EmergencyEvent simulateEmergency(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.simulateEmergency();
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Симуляция аварии не удалась: неверный индекс " + index);
        return null;
    }

    public SystemStatusReport getStatusReport(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            csvLogger.logEvent(system, EventType.INFO, "Запрошен статусный отчет");
            return system.getStatusReport();
        }
        return null;
    }

    public boolean calibrateSensors(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.calibrateSensors();
            return true;
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Калибровка не удалась: неверный индекс " + index);
        return false;
    }

    public boolean checkConnectivity(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.checkConnectivity();
        }
        csvLogger.logSystemEvent(EventType.WARNING, "Проверка подключения не удалась: неверный индекс " + index);
        return false;
    }

    public void logAllSystemsState() {
        csvLogger.logSystemEvent(EventType.INFO, "Логирование состояния всех систем (" + systems.size() + " всего)");
        for (SecuritySystem system : systems) {
            csvLogger.logSystemState(system);
        }
    }

    public void setCSVLogInterval(int seconds) {
        csvLogger.setLogInterval(seconds);
        csvLogger.logSystemEvent(EventType.CONFIG_CHANGED, "Интервал CSV логирования установлен на " + seconds + " секунд");
    }

    public int getSystemCount() {
        return systems.size();
    }

    public boolean hasSystem(int index) {
        return index >= 0 && index < systems.size();
    }

    public CSVLogger getCsvLogger() {
        return csvLogger;
    }

    public String getAllAsString() {
        StringBuilder sb = new StringBuilder();
        for (SecuritySystem sys : systems) {
            sb.append(sys.toString()).append("\n");
        }
        return sb.toString();
    }
    public SecuritySystem getSystemById(String id) {
        for (SecuritySystem sys : systems) {
            if (sys.getSystemId().equals(id)) return sys;
        }
        return null;
    }
    public TextFileParser getTextFileParser() {
        return textFileParser;
    }

    public void saveSystemsToFile(String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (SecuritySystem s : systems) {
                out.println(s.toString()); // toString с форматом "тип поля=знач..."
            }
        } catch (Exception e) {}
    }

    public boolean removeSystemById(String id) {
        Iterator<SecuritySystem> it = systems.iterator();
        while (it.hasNext()) {
            SecuritySystem sys = it.next();
            if (sys.getSystemId().equals(id)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void close() {
        csvLogger.logSystemEvent(EventType.INFO, "Закрытие SecuritySystemController с " + systems.size() + " системами");
        csvLogger.close();
    }
}
