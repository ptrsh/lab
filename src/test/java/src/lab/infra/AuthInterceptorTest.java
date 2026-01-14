package src.lab.infra;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private StringWriter stringWriter;
    private PrintWriter writer;

    private void setupWriter() throws Exception {
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void preHandle_validUuid_returnsTrue() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        when(request.getHeader("Authorization")).thenReturn(validUuid);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request).setAttribute("userId", validUuid);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void preHandle_missingHeader_returnsFalse() throws Exception {
        setupWriter();
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        writer.flush();
        assertTrue(stringWriter.toString().contains("Missing Authorization header"));
    }

    @Test
    void preHandle_emptyHeader_returnsFalse() throws Exception {
        setupWriter();
        when(request.getHeader("Authorization")).thenReturn("");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        writer.flush();
        assertTrue(stringWriter.toString().contains("Missing Authorization header"));
    }

    @Test
    void preHandle_blankHeader_returnsFalse() throws Exception {
        setupWriter();
        when(request.getHeader("Authorization")).thenReturn("   ");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        writer.flush();
        assertTrue(stringWriter.toString().contains("Missing Authorization header"));
    }

    @Test
    void preHandle_invalidUuid_returnsFalse() throws Exception {
        setupWriter();
        when(request.getHeader("Authorization")).thenReturn("not-a-uuid");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        writer.flush();
        assertTrue(stringWriter.toString().contains("Invalid UUID format"));
    }

    @Test
    void preHandle_bearerTokenWithValidUuid_returnsTrue() throws Exception {
        String validUuid = "550e8400-e29b-41d4-a716-446655440000";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validUuid);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request).setAttribute("userId", validUuid);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void preHandle_bearerTokenEmptyAfterTrim_returnsFalse() throws Exception {
        setupWriter();
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
        writer.flush();
        assertTrue(stringWriter.toString().contains("Invalid Authorization header"));
    }
}
