package src.lab.schemas;

public record RedirectResponse(String url) {
    public static RedirectResponse of(String url) {
        return new RedirectResponse(url);
    }
}
