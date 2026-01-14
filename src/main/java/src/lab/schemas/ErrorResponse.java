package src.lab.schemas;

public record ErrorResponse(String error) {
    public static ErrorResponse of(String error) {
        return new ErrorResponse(error);
    }
}
