package com.gogame.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameStatus Enum Tests")
class GameStatusTest {

    @Test
    @DisplayName("should have WAITING_FOR_PLAYERS value")
    void shouldHaveWaitingForPlayersValue() {
        assertEquals("WAITING_FOR_PLAYERS", GameStatus.WAITING_FOR_PLAYERS.name());
    }

    @Test
    @DisplayName("should have IN_PROGRESS value")
    void shouldHaveInProgressValue() {
        assertEquals("IN_PROGRESS", GameStatus.IN_PROGRESS.name());
    }

    @Test
    @DisplayName("should have FINISHED value")
    void shouldHaveFinishedValue() {
        assertEquals("FINISHED", GameStatus.FINISHED.name());
    }

    @Test
    @DisplayName("should have RESIGNED value")
    void shouldHaveResignedValue() {
        assertEquals("RESIGNED", GameStatus.RESIGNED.name());
    }

    @Test
    @DisplayName("should have NEGOTIATING value")
    void shouldHaveNegotiatingValue() {
        assertEquals("NEGOTIATING", GameStatus.NEGOTIATING.name());
    }

    @Test
    @DisplayName("should have exactly 5 values")
    void shouldHaveExactlyFiveValues() {
        assertEquals(5, GameStatus.values().length);
    }

    @Test
    @DisplayName("should return correct value from string")
    void shouldReturnCorrectValueFromString() {
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, GameStatus.valueOf("WAITING_FOR_PLAYERS"));
        assertEquals(GameStatus.IN_PROGRESS, GameStatus.valueOf("IN_PROGRESS"));
        assertEquals(GameStatus.NEGOTIATING, GameStatus.valueOf("NEGOTIATING"));
        assertEquals(GameStatus.FINISHED, GameStatus.valueOf("FINISHED"));
        assertEquals(GameStatus.RESIGNED, GameStatus.valueOf("RESIGNED"));
    }

    @Test
    @DisplayName("should throw exception for invalid value")
    void shouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            GameStatus.valueOf("INVALID");
        });
    }

    @Test
    @DisplayName("all statuses should be unique")
    void allStatusesShouldBeUnique() {
        GameStatus[] values = GameStatus.values();
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertNotEquals(values[i], values[j]);
            }
        }
    }

    @Test
    @DisplayName("should contain expected game lifecycle states")
    void shouldContainExpectedGameLifecycleStates() {
        // Verify the basic game lifecycle is represented
        GameStatus[] expectedStatuses = {
            GameStatus.WAITING_FOR_PLAYERS,  // Initial state
            GameStatus.IN_PROGRESS,          // Game running
            GameStatus.NEGOTIATING,          // Scoring negotiation after two passes
            GameStatus.FINISHED,             // Normal end
            GameStatus.RESIGNED              // Ended by resignation
        };
        
        assertArrayEquals(expectedStatuses, GameStatus.values());
    }
}
