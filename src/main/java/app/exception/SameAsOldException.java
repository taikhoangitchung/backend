package app.exception;

public class SameAsOldException extends RuntimeException {
    public SameAsOldException(String message) {
        super(message);
    }
}
