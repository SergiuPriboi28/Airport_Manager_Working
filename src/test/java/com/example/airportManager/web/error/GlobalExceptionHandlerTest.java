package com.example.airportManager.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// Teste directe pe GlobalExceptionHandler (fara context Spring), la fel ca restul
// testelor din proiect: nu avem nevoie de @WebMvcTest doar ca sa verificam ce status
// si ce corp returneaza fiecare @ExceptionHandler pentru o exceptie data.
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleAccessDenied_shouldReturn403_withGenericMessage() {
        when(request.getRequestURI()).thenReturn("/api/flights/1");
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
        assertThat(response.getBody().code()).isEqualTo("ACCESS_DENIED");
        assertThat(response.getBody().message()).isEqualTo("You do not have permission to perform this action");
        assertThat(response.getBody().path()).isEqualTo("/api/flights/1");
    }

    @Test
    void handleUnreadable_shouldReturn400_onMalformedJson() {
        when(request.getRequestURI()).thenReturn("/api/flights");
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON parse error");

        ResponseEntity<ErrorResponse> response = handler.handleUnreadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo("MALFORMED_REQUEST");
    }

    @Test
    void handleTypeMismatch_shouldReturn400_andNameTheInvalidParameter() {
        when(request.getRequestURI()).thenReturn("/api/flights/abc");
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "abc", Long.class, "id", null, new NumberFormatException("For input string: \"abc\"")
        );

        ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().code()).isEqualTo("TYPE_MISMATCH");
        assertThat(response.getBody().message()).contains("id");
    }
}
