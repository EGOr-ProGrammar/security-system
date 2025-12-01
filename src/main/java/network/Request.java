package network;

import java.util.HashMap;
import java.util.Map;

/**
 * Запрос от клиента к серверу
 */
public class Request {
    private String command;
    private Map<String, Object> params;

    public Request() {
        this.params = new HashMap<>();
    }

    public Request(String command) {
        this.command = command;
        this.params = new HashMap<>();
    }

    public Request(String command, Map<String, Object> params) {
        this.command = command;
        this.params = params;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    @Override
    public String toString() {
        return "Request{command='" + command + "', params=" + params + "}";
    }
}
