package src.lab.schemas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedirectResponseTest {

    @Test
    void of_createsRedirectResponse() {
        RedirectResponse response = RedirectResponse.of("https://example.com");
        assertEquals("https://example.com", response.url());
    }

    @Test
    void record_constructor_setsUrlField() {
        RedirectResponse response = new RedirectResponse("https://test.com");
        assertEquals("https://test.com", response.url());
    }
}
