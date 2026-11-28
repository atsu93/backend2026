package app.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final String secretKey;

    public ApiKeyInterceptor(@Value("${app.security.api-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("GET".equalsIgnoreCase(method) && (path.equals("/api/media") || path.startsWith("/api/media/"))) {
            return true;
        }

        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey == null || !apiKey.equals(secretKey)) {
            throw new UnauthorizedException("Unauthorized: Invalid or missing X-API-KEY");
        }
        return true;
    }
}
