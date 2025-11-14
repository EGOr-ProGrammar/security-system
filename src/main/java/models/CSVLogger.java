package models;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CSVLogger {
    private PrintWriter writer;
    private int logInterval = 10;
    private final String logFilePath = "security_logs.csv";

    public CSVLogger() {
        initializeWriter();
    }

    private void initializeWriter() {
        try {
            File file = new File(logFilePath);
            boolean fileExists = file.exists();

            writer = new PrintWriter(new FileWriter(logFilePath, true));

            if (!fileExists) {
                writer.println("timestamp,system_id,location,security_mode,is_armed,battery_level,signal_strength,event_type,event_description");
            }
        } catch (Exception e) {
            System.err.println("Ошибка инициализации CSV: " + e.getMessage());
        }
    }

    public void logEvent(SecuritySystem system, EventType eventType, String additionalInfo) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().toString();
            String description = eventType.getDescription();
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                description += ": " + additionalInfo;
            }

            String line = String.format("%s,%s,%s,%s,%s,%d,%d,%s,%s",
                    timestamp,
                    system.getSystemId(),
                    system.getLocation(),
                    system.getSecurityMode(),
                    system.isArmed(),
                    system.getBatteryLevel(),
                    system.getSignalStrength(),
                    eventType.name(),
                    description
            );
            writer.println(line);
            writer.flush();
        }
    }

    public void logEvent(SecuritySystem system, EventType eventType) {
        logEvent(system, eventType, null);
    }

    public void logSystemEvent(EventType eventType, String additionalInfo) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().toString();
            String description = eventType.getDescription();
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                description += ": " + additionalInfo;
            }

            String line = String.format("%s,SYSTEM,,,,,,%s,%s",
                    timestamp, eventType.name(), description
            );
            writer.println(line);
            writer.flush();
        }
    }

    @Deprecated
    public void logEvent(String systemId, EventType eventType, String additionalInfo) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().toString();
            String description = eventType.getDescription();
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                description += ": " + additionalInfo;
            }

            String line = String.format("%s,%s,,,,,,%s,%s",
                    timestamp, systemId, eventType.name(), description
            );
            writer.println(line);
            writer.flush();
        }
    }

    @Deprecated
    public void logEvent(String systemId, EventType eventType) {
        logEvent(systemId, eventType, null);
    }

    public void logSystemState(SecuritySystem system) {
        logEvent(system, EventType.STATE_UPDATE);
    }

    public List<String> getRecentLogs(int maxLines) {
        List<String> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            reader.readLine(); // Пропустить заголовок
            while ((line = reader.readLine()) != null) {
                logs.add(line);
            }

            int startIndex = Math.max(0, logs.size() - maxLines);
            return logs.subList(startIndex, logs.size());
        } catch (IOException e) {
            System.err.println("Ошибка чтения логов: " + e.getMessage());
            return logs;
        }
    }

    public List<String> getLogsBySystemId(String systemId, int maxLines) {
        List<String> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            reader.readLine(); // Пропустить заголовок
            while ((line = reader.readLine()) != null) {
                if (line.contains(systemId)) {
                    logs.add(line);
                }
            }

            int startIndex = Math.max(0, logs.size() - maxLines);
            return logs.subList(startIndex, logs.size());
        } catch (IOException e) {
            System.err.println("Ошибка чтения логов: " + e.getMessage());
            return logs;
        }
    }

    public void setLogInterval(int seconds) {
        this.logInterval = seconds;
    }

    public int getLogInterval() {
        return logInterval;
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
