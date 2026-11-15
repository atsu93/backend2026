package app.api;

import app.api.dto.MediaDto;
import app.api.dto.ProfileDto;
import app.api.dto.WatchRequest;
import app.service.StreamingException;
import app.service.StreamingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class StreamingHttpHandler implements HttpHandler {
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

            if (path.equals("/api/profiles") || path.startsWith("/api/profiles/")) {
                handleProfiles(exchange, method, path);
            } else if (path.equals("/api/media") || path.startsWith("/api/media/")) {
                handleMedia(exchange, method, path);
            } else if (path.equals("/api/watch")) {
                if (!"POST".equals(method)) {
                    sendMethodNotAllowed(exchange, "POST");
                    return;
                }
                handleWatch(exchange);
            } else if (path.equals("/api/upgrade")) {
                if (!"POST".equals(method)) {
                    sendMethodNotAllowed(exchange, "POST");
                    return;
                }
                handleUpgrade(exchange);
            } else if (path.equals("/api/topup")) {
                if (!"POST".equals(method)) {
                    sendMethodNotAllowed(exchange, "POST");
                    return;
                }
                handleTopUp(exchange);
            } else {
                sendResponse(exchange, 404, Map.of("error", "Not Found"));
            }
        } catch (StreamingException e) {
            sendResponse(exchange, e.getStatusCode(), Map.of("error", e.getMessage()));
        } catch (JacksonException e) {
            sendResponse(exchange, 400, Map.of("error", "Invalid JSON format: " + e.getMessage()));
        } catch (Exception e) {
            if (exchange.getResponseCode() == -1) {
                e.printStackTrace();
                sendResponse(exchange, 500, Map.of("error", "Internal Server Error: "));
            } else {
                exchange.close();
            }
        }
    }


    private void handleProfiles(HttpExchange exchange, String method, String path) throws IOException {
        checkAuth(exchange);

        String[] parts = path.split("/");
        if (parts.length == 3) {
            switch (method) {
                case "GET" -> sendResponse(exchange, 200, streamingService.getAllProfiles());
                case "POST" -> {
                    ProfileDto dto = readBody(exchange, ProfileDto.class);
                    if (dto.name == null || dto.name.isBlank()) {
                        sendResponse(exchange, 400, Map.of("error", "Name cannot be empty"));
                        return;
                    }
                    sendResponse(exchange, 201, streamingService.createProfile(dto.name));
                }
                default -> sendMethodNotAllowed(exchange, "GET, POST");
            }
        } else if (parts.length == 4) {
            String id = parts[3];
            if (id.isBlank()) {
                sendResponse(exchange, 400, Map.of("error", "Profile ID cannot be blank"));
                return;
            }
            switch (method) {
                case "GET" -> sendResponse(exchange, 200, streamingService.getProfile(id));
                case "PUT" -> {
                    ProfileDto dto = readBody(exchange, ProfileDto.class);
                    if (dto.name != null && dto.name.isBlank()) {
                        sendResponse(exchange, 400, Map.of("error", "Name cannot be blank"));
                        return;
                    }
                    sendResponse(exchange, 200, streamingService.updateProfile(id, dto.name));
                }
                case "DELETE" -> {
                    streamingService.deleteProfile(id);
                    sendResponse(exchange, 204, null);
                }
                default -> sendMethodNotAllowed(exchange, "GET, PUT, DELETE");
            }
        } else {
            sendResponse(exchange, 404, Map.of("error", "Not Found"));
        }
    }

    private void handleMedia(HttpExchange exchange, String method, String path) throws IOException {
        String[] parts = path.split("/");
        if (parts.length == 3) {
            switch (method) {
                case "GET" -> {
                    Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());
                    String rating = queryParams.getOrDefault("ageRating", queryParams.get("rating"));
                    Boolean onlyFree = parseBooleanParam(queryParams, "onlyFree");
                    sendResponse(exchange, 200, streamingService.getMediaFiltered(rating, onlyFree));
                }
                case "POST" -> {
                    checkAuth(exchange);
                    MediaDto dto = readBody(exchange, MediaDto.class);
                    if (dto.title == null || dto.title.isBlank() || dto.ageRating == null || dto.ageRating.isBlank()) {
                        sendResponse(exchange, 400, Map.of("error", "Title and ageRating cannot be empty"));
                        return;
                    }
                    boolean isPremium = dto.isPremium != null && dto.isPremium;
                    sendResponse(exchange, 201, streamingService.createMedia(dto.title, dto.ageRating, isPremium));
                }
                default -> sendMethodNotAllowed(exchange, "GET, POST");
            }
        } else if (parts.length == 4) {
            String id = parts[3];
            if (id.isBlank()) {
                sendResponse(exchange, 400, Map.of("error", "Media ID cannot be blank"));
                return;
            }
            switch (method) {
                case "GET" -> sendResponse(exchange, 200, streamingService.getMedia(id));
                case "PUT" -> {
                    checkAuth(exchange);
                    MediaDto dto = readBody(exchange, MediaDto.class);
                    if (dto.title != null && dto.title.isBlank()) {
                        sendResponse(exchange, 400, Map.of("error", "Title cannot be blank"));
                        return;
                    }
                    if (dto.ageRating != null && dto.ageRating.isBlank()) {
                        sendResponse(exchange, 400, Map.of("error", "Age rating cannot be blank"));
                        return;
                    }
                    sendResponse(exchange, 200, streamingService.updateMedia(id, dto.title, dto.ageRating, dto.isPremium));
                }
                case "DELETE" -> {
                    checkAuth(exchange);
                    streamingService.deleteMedia(id);
                    sendResponse(exchange, 204, null);
                }
                default -> sendMethodNotAllowed(exchange, "GET, PUT, DELETE");
            }
        } else {
            sendResponse(exchange, 404, Map.of("error", "Not Found"));
        }
    }

    private void handleWatch(HttpExchange exchange) throws IOException {
        checkAuth(exchange);
        WatchRequest req = readBody(exchange, WatchRequest.class);
        if (req.profileId == null || req.profileId.isBlank() || req.mediaId == null || req.mediaId.isBlank()) {
            sendResponse(exchange, 400, Map.of("error", "profileId and mediaId fields are required"));
            return;
        }
        streamingService.watchMedia(req.profileId, req.mediaId);
        sendResponse(exchange, 200, Map.of("status", "Successfully updated views"));
    }

    @SuppressWarnings("unchecked")
    private void handleUpgrade(HttpExchange exchange) throws IOException {
        checkAuth(exchange);
        Map<String, Object> req = readBody(exchange, Map.class);
        String profileId = req.get("profileId") != null ? req.get("profileId").toString() : null;
        if (profileId == null || profileId.isBlank()) {
            sendResponse(exchange, 400, Map.of("error", "profileId field is required"));
            return;
        }
        streamingService.buyPremium(profileId);
        sendResponse(exchange, 200, Map.of("status", "Premium status successfully purchased!"));
    }

    @SuppressWarnings("unchecked")
    private void handleTopUp(HttpExchange exchange) throws IOException {
        checkAuth(exchange);
        Map<String, Object> req = readBody(exchange, Map.class);
        String profileId = req.get("profileId") != null ? req.get("profileId").toString() : null;
        if (profileId == null || profileId.isBlank() || req.get("amount") == null) {
            sendResponse(exchange, 400, Map.of("error", "profileId and amount fields are required"));
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(req.get("amount").toString());
        } catch (NumberFormatException e) {
            sendResponse(exchange, 400, Map.of("error", "amount must be a valid number"));
            return;
        }
        streamingService.topUpBalance(profileId, amount);
        sendResponse(exchange, 200, Map.of("status", "Balance successfully topped up!"));
    }

    private void checkAuth(HttpExchange exchange) {
        String apiKey = exchange.getRequestHeaders().getFirst("X-API-KEY");
        if (apiKey == null || !apiKey.equals(SECRET_KEY)) {
            throw new StreamingException("Unauthorized: Invalid or missing X-API-KEY", 401);
        }
    }

    private <T> T readBody(HttpExchange exchange, Class<T> clazz) throws IOException {
        byte[] rawBody = exchange.getRequestBody().readAllBytes();
        if (rawBody.length == 0) {
            throw new StreamingException("Request body is required", 400);
        }
        T dto = objectMapper.readValue(rawBody, clazz);
        if (dto == null) {
            throw new StreamingException("Request body is required", 400);
        }
        return dto;
    }

    private Boolean parseBooleanParam(Map<String, String> queryParams, String name) {
        if (!queryParams.containsKey(name)) {
            return null;
        }
        String value = queryParams.get(name);
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        throw new StreamingException("Parameter '" + name + "' must be 'true' or 'false'", 400);
    }

    private void sendMethodNotAllowed(HttpExchange exchange, String allowedMethods) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethods);
        sendResponse(exchange, 405, Map.of("error", "Method Not Allowed"));
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
        if (query == null || query.isEmpty()) {
            return result;
        }
        for (String param : query.split("&")) {
            if (param.isEmpty()) {
                continue;
            }
            String[] entry = param.split("=", 2);
            String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8);
            String value = entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : "";
            result.put(key, value);
        }
        return result;
    }
}

