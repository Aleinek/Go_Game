package student.pwr.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import student.pwr.dto.PlayerResponse;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class APIControllerTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private APIController apiController;

    @BeforeEach
    void setUp() {
        apiController = new APIController("http://test-server", mockHttpClient);
    }

    @Test
    void registerPlayerShouldReturnPlayerWhenServerReturns201() throws Exception {
        // GIVEN
        String jsonResponse = "{\"id\": \"123e4567-e89b-12d3-a456-426614174000\", \"nickname\": \"Krzysztof\", \"capturedStones\": 0}";
        
        when(mockResponse.statusCode()).thenReturn(201);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        // WHEN
        PlayerResponse result = apiController.registerPlayer("Krzysztof");

        // THEN
        assertNotNull(result);
        assertEquals("Krzysztof", result.nickname());
    }

    @Test
    void registerPlayerShouldThrowExceptionWhenServerReturnsError() throws Exception {
        // GIVEN
        when(mockResponse.statusCode()).thenReturn(400); 
        when(mockResponse.body()).thenReturn("Nickname taken");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockResponse);

        // WHEN & THEN
        Exception exception = assertThrows(RuntimeException.class, () -> {
            apiController.registerPlayer("ExistingUser");
        });

        assertTrue(exception.getMessage().contains("Blad rejestracji"));
    }
}
