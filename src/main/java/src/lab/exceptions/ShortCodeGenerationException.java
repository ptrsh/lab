package src.lab.exceptions;

public class ShortCodeGenerationException extends RuntimeException {
    public ShortCodeGenerationException() {
        super("Unable to generate unique short code after multiple attempts");
    }
}
