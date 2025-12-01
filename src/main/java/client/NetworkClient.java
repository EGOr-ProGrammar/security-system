package client;

import com.google.gson.Gson;
import network.Request;
import network.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkClient {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson;
    private boolean connected;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.gson = new Gson();
        this.connected = false;
    }

    public boolean connect() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            connected = true;
            System.out.println("Подключено к серверу " + host + ":" + port);
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
            connected = false;
            return false;
        }
    }

    public Response sendRequest(Request request) throws IOException {
        if (!connected) {
            throw new IOException("Нет подключения к серверу");
        }

        try {
            // Отправка запроса
            String jsonRequest = gson.toJson(request);
            out.println(jsonRequest);

            // Получение ответа
            String jsonResponse = in.readLine();

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new IOException("Сервер закрыл соединение или вернул пустой ответ");
            }

            return gson.fromJson(jsonResponse, Response.class);

        } catch (IOException e) {
            connected = false;
            throw e;
        }
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            connected = false;
            System.out.println("Отключено от сервера");
        } catch (IOException e) {
            System.err.println("Ошибка при закрытии соединения: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    public Gson getGson() {
        return gson;
    }
}
