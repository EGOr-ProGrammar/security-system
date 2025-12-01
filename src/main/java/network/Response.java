package network;

/**
 * Ответ сервера клиенту
 */
public class Response {
    private boolean success;
    private String message;
    private Object data;

    public Response() {
    }

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Response(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static Response success(String message) {
        return new Response(true, message);
    }

    public static Response success(String message, Object data) {
        return new Response(true, message, data);
    }

    public static Response error(String message) {
        return new Response(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Response{success=" + success + ", message='" + message + "', data=" + data + "}";
    }
}
