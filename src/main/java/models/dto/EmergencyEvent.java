package models.dto;

import java.time.LocalDateTime;

/**
 * Представляет событие тревоги в системе безопасности.
 */
public class EmergencyEvent {
    private final String systemId;
    private final String systemType;
    private final String eventType;
    private final String description;
    private final LocalDateTime timestamp;
    private final boolean requiresResponse;

    public EmergencyEvent(String systemId, String systemType, String eventType,
                          String description, boolean requiresResponse) {
        this.systemId = systemId;
        this.systemType = systemType;
        this.eventType = eventType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.requiresResponse = requiresResponse;
    }

    public String getSystemId() {
        return systemId;
    }

    public String getSystemType() {
        return systemType;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRequiresResponse() {
        return requiresResponse;
    }
}
