package app.service;

public class StreamingException extends RuntimeException {
    private final int statusCode;

    public StreamingException(String message, int statusCode) {
        super(message);
        if (statusCode < 400 || statusCode > 599) {
            throw new IllegalArgumentException("Invalid HTTP error status code: " + statusCode);
        }
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}