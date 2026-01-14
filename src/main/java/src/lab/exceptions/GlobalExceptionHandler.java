package src.lab.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import src.lab.schemas.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LinkNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleLinkNotFound(LinkNotFoundException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(LinkExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ErrorResponse handleLinkExpired(LinkExpiredException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(LinkLimitReachedException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ErrorResponse handleLinkLimitReached(LinkLimitReachedException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(InvalidUrlException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidUrl(InvalidUrlException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleShortCodeGeneration(ShortCodeGenerationException ex) {
        return ErrorResponse.of(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(error -> error.getDefaultMessage())
            .orElse("Validation failed");
        return ErrorResponse.of(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        return ErrorResponse.of("An unexpected error occurred: " + ex.getMessage());
    }
}
