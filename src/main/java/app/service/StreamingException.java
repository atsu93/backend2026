package app.service;

public class StreamingException extends RuntimeException{
    private final int statusCode;

    public  StreamingException(String message, int statusCode){
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode(){return statusCode;}
}
