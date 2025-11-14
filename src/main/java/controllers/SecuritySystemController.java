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

    // Добавление системы с логированием
    public void addSystem(SecuritySystem system) {
        system.setCsvLogger(csvLogger);
        systems.add(system);
        csvLogger.logEvent(system.getSystemId(), EventType.SYSTEM_ADDED);
    }

    // Удаление системы с логированием
    public boolean removeSystem(int index) {
        if (index >= 0 && index < systems.size()) {
            SecuritySystem removed = systems.remove(index);
            csvLogger.logEvent(removed.getSystemId(), EventType.SYSTEM_REMOVED);
            return true;
        }
        return false;
    }

    // Получение системы по индексу
    public SecuritySystem getSystem(int index) {
        return (index >= 0 && index < systems.size()) ? systems.get(index) : null;
    }

    // Получение всех систем (копия для безопасности)
    public List<SecuritySystem> getAllSystems() {
        return new ArrayList<>(systems);
    }

    // Загрузка систем из файла с логированием
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

    // Смена текущего файла с логированием
    public void setFileName(String fileName) {
        String oldFile = this.currentFileName;
        this.currentFileName = fileName;
        csvLogger.logEvent("SYSTEM", EventType.CONFIG_CHANGED, "Файл изменен с " + oldFile + " на " + fileName);
    }

    // Получение имени текущего файла
    public String getCurrentFileName() {
        return currentFileName;
    }

    // Постановка системы на охрану
    public boolean armSystem(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.armSystem();
            return true;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Постановка на охрану не удалась: неверный индекс " + index);
        return false;
    }

    // Снятие системы с охраны
    public boolean disarmSystem(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.disarmSystem();
            return true;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Снятие с охраны не удалось: неверный индекс " + index);
        return false;
    }

    // Изменение режима безопасности с логированием
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

    // Выполнение самодиагностики с логированием результата
    public boolean performSelfTest(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            boolean result = system.performSelfTest();
            return result;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Самодиагностика не удалась: неверный индекс " + index);
        return false;
    }

    // Симуляция аварии
    public String simulateEmergency(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.simulateEmergency();
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Симуляция аварии не удалась: неверный индекс " + index);
        return "Ошибка: система не найдена";
    }

    // Получение статусного отчета
    public String getStatusReport(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            csvLogger.logEvent(system.getSystemId(), EventType.INFO, "Запрошен статусный отчет");
            return system.getStatusReport();
        }
        return "Ошибка: система не найдена";
    }

    // Калибровка сенсоров
    public boolean calibrateSensors(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            system.calibrateSensors();
            return true;
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Калибровка не удалась: неверный индекс " + index);
        return false;
    }

    // Проверка подключения
    public boolean checkConnectivity(int index) {
        SecuritySystem system = getSystem(index);
        if (system != null) {
            return system.checkConnectivity();
        }
        csvLogger.logEvent("SYSTEM", EventType.WARNING, "Проверка подключения не удалась: неверный индекс " + index);
        return false;
    }

    // Логирование состояния всех систем
    public void logAllSystemsState() {
        csvLogger.logEvent("SYSTEM", EventType.INFO, "Логирование состояния всех систем (" + systems.size() + " всего)");
        for (SecuritySystem system : systems) {
            csvLogger.logSystemState(system);
        }
    }

    // Установка интервала логирования
    public void setCSVLogInterval(int seconds) {
        csvLogger.setLogInterval(seconds);
        csvLogger.logEvent("SYSTEM", EventType.CONFIG_CHANGED, "Интервал CSV логирования установлен на " + seconds + " секунд");
    }

    // Получение количества систем
    public int getSystemCount() {
        return systems.size();
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
