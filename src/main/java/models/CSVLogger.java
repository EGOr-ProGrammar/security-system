package models;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public class CSVLogger {
    private PrintWriter writer;
    private int logInterval = 10;

    public CSVLogger() {
        initializeWriter();
    }

    private void initializeWriter() {
        try {
            writer = new PrintWriter(new FileWriter("security_logs.csv", true));
            writer.println("timestamp,system_id,location,security_mode,is_armed,battery_level,signal_strength,event_type,event_description");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации CSV: " + e.getMessage());
        }
    }

    public void logSystemState(SecuritySystem system) {
        if (writer != null) {
            String timestamp = LocalDateTime.now().toString();
            String line = String.format("%s,%s,%s,%s,%s,%d,%d,%s,%s",
                    timestamp, system.getSystemId(), system.getLocation(),
                    system.getSecurityMode(), system.isArmed(),
                    system.getBatteryLevel(), system.getSignalStrength(),
                    EventType.STATE_UPDATE.name(), EventType.STATE_UPDATE.getDescription()
            );
            writer.println(line);
            writer.flush();
        }
    }

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

    // Перегрузка для обратной совместимости
    public void logEvent(String systemId, EventType eventType) {
        logEvent(systemId, eventType, null);
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
