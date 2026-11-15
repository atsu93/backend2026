package app;

import app.api.StreamingHttpHandler;
import app.service.StreamingService;
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

        server.createContext("/api", new StreamingHttpHandler(streamingService));
        server.start();
        System.out.println("Сервер запущен: http://localhost:" + PORT + "/api");
    }
}
