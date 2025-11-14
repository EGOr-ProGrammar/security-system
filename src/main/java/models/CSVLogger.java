package models;

import config.ConfigManager;
import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CSVLogger {
    private final ConfigManager config = ConfigManager.getInstance();
    private PrintWriter writer;
    private int logInterval = 10;
    private final String logFilePath;

    public CSVLogger() {
        this.logFilePath = config.getString("file.csv.log");
        initializeWriter();
    }

    private void initializeWriter() {
        try {
            File file = new File(logFilePath);
            boolean fileExists = file.exists();
            writer = new PrintWriter(new FileWriter(logFilePath, true));
            if (!fileExists) {
                writer.println(config.getString("csv.header"));
            }
        } catch (Exception e) {
            System.err.println(config.getString("error.prefix") + " CSV: " + e.getMessage());
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
                    description);
            writer.println(line);
            writer.flush();
        }
    }

    public void logEvent(SecuritySystem system, EventType eventType) {
        logEvent(system, eventType, "");
    }

    public void logSystemEvent(EventType eventType, String details) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().toString();
            String description = eventType.getDescription();
            if (details != null && !details.isEmpty()) {
                description += ": " + details;
            }

            String line = String.format("%s,%s,%s,%s,%s,%d,%d,%s,%s",
                    timestamp,
                    "SYSTEM",
                    "N/A",
                    "N/A",
                    false,
                    0,
                    0,
                    eventType.name(),
                    description);
            writer.println(line);
            writer.flush();
        }
    }

    public void logSystemState(SecuritySystem system) {
        logEvent(system, EventType.STATE_UPDATE);
    }

    public void setLogInterval(int seconds) {
        this.logInterval = seconds;
    }

    public int getLogInterval() {
        return logInterval;
    }

    public List<String> getRecentLogs(int count) {
        List<String> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            List<String> allLogs = new ArrayList<>();
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                allLogs.add(line);
            }
            int start = Math.max(0, allLogs.size() - count);
            logs = allLogs.subList(start, allLogs.size());
        } catch (Exception e) {
            System.err.println(config.getString("error.prefix") + " " + e.getMessage());
        }
        return logs;
    }

    public List<String> getLogsBySystemId(String systemId, int count) {
        List<String> logs = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            List<String> systemLogs = new ArrayList<>();
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[1].equals(systemId)) {
                    systemLogs.add(line);
                }
            }
            int start = Math.max(0, systemLogs.size() - count);
            logs = systemLogs.subList(start, systemLogs.size());
        } catch (Exception e) {
            System.err.println(config.getString("error.prefix") + " " + e.getMessage());
        }
        return logs;
    }

    public void close() {
        if (writer != null) {
            writer.close();
        }
    }

    public String getLogFilePath() {
        return logFilePath;
    }
}
