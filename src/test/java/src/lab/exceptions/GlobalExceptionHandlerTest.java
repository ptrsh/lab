package src.lab.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import src.lab.schemas.ErrorResponse;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleLinkNotFound_returnsErrorResponse() {
        LinkNotFoundException ex = new LinkNotFoundException("abc123");
        ErrorResponse response = handler.handleLinkNotFound(ex);
        assertEquals("Link not found: abc123", response.error());
    }

    @Test
    void handleLinkExpired_returnsErrorResponse() {
        LinkExpiredException ex = new LinkExpiredException("abc123");
        ErrorResponse response = handler.handleLinkExpired(ex);
        assertEquals("Link has expired: abc123", response.error());
    }

    @Test
    void handleLinkLimitReached_returnsErrorResponse() {
        LinkLimitReachedException ex = new LinkLimitReachedException("abc123");
        ErrorResponse response = handler.handleLinkLimitReached(ex);
        assertEquals("Click limit reached for link: abc123", response.error());
    }

    @Test
    void handleUnauthorizedAccess_returnsErrorResponse() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Not authorized");
        ErrorResponse response = handler.handleUnauthorizedAccess(ex);
        assertEquals("Not authorized", response.error());
    }

    @Test
    void handleInvalidUrl_returnsErrorResponse() {
        InvalidUrlException ex = new InvalidUrlException("bad-url");
        ErrorResponse response = handler.handleInvalidUrl(ex);
        assertEquals("Invalid URL format: bad-url", response.error());
    }

    @Test
    void handleShortCodeGeneration_returnsErrorResponse() {
        ShortCodeGenerationException ex = new ShortCodeGenerationException();
        ErrorResponse response = handler.handleShortCodeGeneration(ex);
        assertEquals("Unable to generate unique short code after multiple attempts", response.error());
    }

    @Test
    void handleValidationErrors_returnsErrorResponse() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "url", "URL is required");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ErrorResponse response = handler.handleValidationErrors(ex);
        assertEquals("URL is required", response.error());
    }

    @Test
    void handleValidationErrors_noFieldErrors_returnsDefaultMessage() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ErrorResponse response = handler.handleValidationErrors(ex);
        assertEquals("Validation failed", response.error());
    }

    @Test
    void handleGenericException_returnsErrorResponse() {
        Exception ex = new RuntimeException("Unexpected error");
        ErrorResponse response = handler.handleGenericException(ex);
        assertEquals("An unexpected error occurred: Unexpected error", response.error());
    }
}
