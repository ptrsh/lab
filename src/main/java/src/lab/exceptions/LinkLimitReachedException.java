package src.lab.exceptions;

public class LinkLimitReachedException extends RuntimeException {
    public LinkLimitReachedException(String shortCode) {
        super("Click limit reached for link: " + shortCode);
    }
}
