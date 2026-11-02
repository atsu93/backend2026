package app.api;

import app.api.dto.MediaDto;
import app.api.dto.ProfileDto;
import app.api.dto.WatchRequest;
import app.service.StreamingException;
import app.service.StreamingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class StreamingHttpHandler implements HttpHandler{
    private final StreamingService streamingService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SECRET_KEY = "super-secret-token-123";

    public StreamingHttpHandler(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (path.startsWith("/api/profiles")) {
                handleProfiles(exchange, method, path);
            } else if (path.startsWith("/api/media")) {
                handleMedia(exchange, method, path);
            } else if (path.equals("/api/watch") && "POST".equals(method)) {
                handleWatch(exchange);
            } else if (path.equals("/api/upgrade") && "POST".equals(method)) {
                handleUpgrade(exchange);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Not Found"));
            }
        } catch (StreamingException e) {
            sendResponse(exchange, e.getStatusCode(), Map.of("error", e.getMessage()));
        } catch (Exception e) {
            sendResponse(exchange, 400, Map.of("error", "Bad Request: " + e.getMessage()));
        }
    }

    private void handleProfiles(HttpExchange exchange, String method, String path) throws IOException {
        String apiKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
        if (apiKey == null || !apiKey.equals(SECRET_KEY)) {
            sendResponse(exchange, 401, Map.of("error", "Unauthorized: Invalid or missing X-API-KEY"));
            return;
        }

        String[] parts = path.split("/");
        if (method.equals("GET") && parts.length == 3) {
            sendResponse(exchange, 200, streamingService.getAllProfiles());
        } else if (method.equals("POST") && parts.length == 3) {
            ProfileDto dto = objectMapper.readValue(exchange.getRequestBody(), ProfileDto.class);
            if (dto.name == null || dto.type == null) {
                sendResponse(exchange, 400, Map.of("error", "Missing fields"));
                return;
            }
            sendResponse(exchange, 201, streamingService.createProfile(dto.name, dto.type));
        } else if (parts.length == 4) {
            String id = parts[3];
            switch (method) {
                case "GET" -> sendResponse(exchange, 200, streamingService.getProfile(id));
                case "PUT" -> {
                    ProfileDto dto = objectMapper.readValue(exchange.getRequestBody(), ProfileDto.class);
                    sendResponse(exchange, 200, streamingService.updateProfile(id, dto.name, dto.type));
                }
                case "DELETE" -> {
                    streamingService.deleteProfile(id);
                    sendResponse(exchange, 204, null);
                }
            }
        }
    }

    private void handleMedia(HttpExchange exchange, String method, String path) throws IOException {
        String[] parts = path.split("/");
        if (method.equals("GET") && parts.length == 3) {
            Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());
            String rating = queryParams.get("rating");
            Boolean onlyFree = queryParams.containsKey("onlyFree") ? Boolean.parseBoolean(queryParams.get("onlyFree")) : null;
            sendResponse(exchange, 200, streamingService.getMediaFiltered(rating, onlyFree));
        } else if (method.equals("POST") && parts.length == 3) {
            MediaDto dto = objectMapper.readValue(exchange.getRequestBody(), MediaDto.class);
            if (dto.title == null || dto.ageRating == null) {
                sendResponse(exchange, 400, Map.of("error", "Missing fields"));
                return;
            }
            sendResponse(exchange, 201, streamingService.createMedia(dto.title, dto.ageRating, dto.isPremium));
        } else if (parts.length == 4) {
            String id = parts[3];
            switch (method) {
                case "GET" -> sendResponse(exchange, 200, streamingService.getMedia(id));
                case "PUT" -> {
                    MediaDto dto = objectMapper.readValue(exchange.getRequestBody(), MediaDto.class);
                    sendResponse(exchange, 200, streamingService.updateMedia(id, dto.title, dto.ageRating, dto.isPremium));
                }
                case "DELETE" -> {
                    streamingService.deleteMedia(id);
                    sendResponse(exchange, 204, null);
                }
            }
        }
    }

    private void handleWatch(HttpExchange exchange) throws IOException {
        WatchRequest req = objectMapper.readValue(exchange.getRequestBody(), WatchRequest.class);
        if (req.profileId == null || req.mediaId == null) {
            sendResponse(exchange, 400, Map.of("error", "profileId and mediaId fields are required"));
            return;
        }
        streamingService.watchMedia(req.profileId, req.mediaId);
        sendResponse(exchange, 200, Map.of("status", "Successfully updated views"));
    }

    private void handleUpgrade(HttpExchange exchange) throws IOException {
        Map<String, String> req = objectMapper.readValue(exchange.getRequestBody(), Map.class);
        String profileId = req.get("profileId");
        if (profileId == null) {
            sendResponse(exchange, 400, Map.of("error", "profileId field is required"));
            return;
        }
        streamingService.buyPremium(profileId);
        sendResponse(exchange, 200, Map.of("status", "Premium status successfully purchased!"));
    }

    private void sendResponse(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = new byte[0];
        if (body != null) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            bytes = objectMapper.writeValueAsBytes(body);
        }
        exchange.sendResponseHeaders(statusCode, bytes.length == 0 && statusCode != 204 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        exchange.close();
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isEmpty()) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(URLDecoder.decode(entry[0], StandardCharsets.UTF_8), URLDecoder.decode(entry[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

}
