package src.lab.exceptions;

public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String shortCode) {
        super("Link has expired: " + shortCode);
    }
}
