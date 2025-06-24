package app.exception;

public class NotActiveException extends RuntimeException {
    public NotActiveException(String message) {
        super(message);
    }
}
