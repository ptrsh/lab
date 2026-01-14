package src.lab.schemas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseTest {

    @Test
    void of_createsErrorResponse() {
        ErrorResponse response = ErrorResponse.of("Test error");
        assertEquals("Test error", response.error());
    }

    @Test
    void record_constructor_setsErrorField() {
        ErrorResponse response = new ErrorResponse("Another error");
        assertEquals("Another error", response.error());
    }
}
