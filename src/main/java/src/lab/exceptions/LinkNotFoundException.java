package src.lab.exceptions;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String shortCode) {
        super("Link not found: " + shortCode);
    }
}
