// controllers/SecuritySystemController.java
package controllers;

import models.*;
import models.CSVLogger;
import java.util.ArrayList;
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
        csvLogger.logEvent(system.getSystemId(), EventType.SYSTEM_ADDED);
    }

    public boolean removeSystem(int index) {
        if (index >= 0 && index < systems.size()) {
            SecuritySystem removed = systems.remove(index);
            csvLogger.logEvent(removed.getSystemId(), EventType.SYSTEM_REMOVED);
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
                csvLogger.logEvent("SYSTEM", EventType.INFO, "Очистка существующих систем");
            }

            List<SecuritySystem> loadedSystems = textFileParser.readFromFile(fileName);

            for (SecuritySystem system : loadedSystems) {
                system.setCsvLogger(csvLogger);
                systems.add(system);
                csvLogger.logEvent(system.getSystemId(), EventType.SYSTEM_LOADED, "Из файла: " + fileName);
            }

            this.currentFileName = fileName;
            csvLogger.logEvent("SYSTEM", EventType.INFO, "Загружено " + loadedSystems.size() + " систем из " + fileName);
            return true;

        } catch (Exception e) {
            csvLogger.logEvent("SYSTEM", EventType.ERROR, "Ошибка загрузки из " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public void setFileName(String fileName) {
        String oldFile = this.currentFileName;
        this.currentFileName = fileName;
        csvLogger.logEvent("SYSTEM", EventType.CONFIG_CHANGED, "Файл изменен с " + oldFile + " на " + fileName);
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
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Постановка на охрану не удалась: неверный индекс " + index);
        return false;
    }

    public boolean disarmSystem(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.disarmSystem();
            return true;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Снятие с охраны не удалось: неверный индекс " + index);
        return false;
    }

    public boolean setSecurityMode(int index, String mode) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            try {
                system.setSecurityMode(mode);
                return true;
            } catch (IllegalArgumentException e) {
                csvLogger.logEvent(system.getSystemId(), EventType.ERROR, "Недопустимый режим: " + mode);
                return false;
            }
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Изменение режима не удалось: неверный индекс " + index);
        return false;
    }

    public boolean performSelfTest(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            boolean result = system.performSelfTest();
            return result;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Самодиагностика не удалась: неверный индекс " + index);
        return false;
    }

    public String simulateEmergency(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.simulateEmergency();
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Симуляция аварии не удалась: неверный индекс " + index);
        return "Ошибка: система не найдена";
    }

    public String getStatusReport(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            csvLogger.logEvent(system.getSystemId(), EventType.INFO, "Запрошен статусный отчет");
            return system.getStatusReport();
        }
        return "Ошибка: система не найдена";
    }

    public boolean calibrateSensors(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.calibrateSensors();
            return true;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Калибровка не удалась: неверный индекс " + index);
        return false;
    }

    public boolean checkConnectivity(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.checkConnectivity();
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Проверка подключения не удалась: неверный индекс " + index);
        return false;
    }

    public void logAllSystemsState() {
        csvLogger.logEvent("SYSTEM", EventType.INFO, "Логирование состояния всех систем (" + systems.size() + " всего)");
        for (SecuritySystem system : systems) {
            csvLogger.logSystemState(system);
        }
    }

    public void setCSVLogInterval(int seconds) {
        csvLogger.setLogInterval(seconds);
        csvLogger.logEvent("SYSTEM", EventType.CONFIG_CHANGED, "Интервал CSV логирования установлен на " + seconds + " секунд");
    }

    public int getSystemCount() {
        return systems.size();
    }

    public CSVLogger getCsvLogger() {
        return csvLogger;
    }

    // Проверка существования системы по индексу
    public boolean hasSystem(int index) {
        return index >= 0 && index < systems.size();
    }

    // Закрытие ресурсов (CSV файла)
    public void close() {
        csvLogger.logEvent("SYSTEM", EventType.INFO, "Закрытие SecuritySystemController с " + systems.size() + " системами");
        csvLogger.close();
    }
}
