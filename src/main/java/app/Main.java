package app;

import app.api.*;
import app.service.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        StreamingService streamingService = new StreamingService();

        streamingService.createMedia("SpongeBob SquarePants", "G", false);
        streamingService.createMedia("Stranger Things", "PG-13", false);
        streamingService.createMedia("The Witcher", "R", true);

        // Каждый контекст — это префикс пути и обработчик для него.
        // Внимание: контекст "/ping" получит и запросы на "/ping/что-угодно".
        //server.createContext("/ping", new PingHandler());

        server.createContext("/api", new StreamingHttpHandler(streamingService));

        // TODO: зарегистрируйте здесь обработчики своих ресурсов

        server.start();
        System.out.println("Сервер запущен: http://localhost:" + PORT + "/api");
    }
}
