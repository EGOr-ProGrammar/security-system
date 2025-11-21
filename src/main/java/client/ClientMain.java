package client;

import java.net.*;
import java.io.*;
import java.util.Properties;

public class ClientMain {
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        try (InputStream in = ClientMain.class.getClassLoader().getResourceAsStream("application.properties")) {
            props.load(in);
        }
        String ip = props.getProperty("ip", "127.0.0.1");
        int port = Integer.parseInt(props.getProperty("port", "5000"));
        try (Socket socket = new Socket(ip, port);
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Введите команду (LIST, GET id=..., ADD ...):");

            while (true) {
                String cmd = userIn.readLine();
                out.println(cmd);
                String resp = in.readLine();
                System.out.println("Ответ сервера:");
                System.out.println(resp);
                if ("GOODBYE".equalsIgnoreCase(resp)) break;
            }
        }
    }
}

