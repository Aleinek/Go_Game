package com.gogame.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DeadStoneStatus Enum Tests")
class DeadStoneStatusTest {

    @Test
    @DisplayName("should have ALIVE value")
    void shouldHaveAliveValue() {
        assertEquals("ALIVE", DeadStoneStatus.ALIVE.name());
    }

    @Test
    @DisplayName("should have DEAD value")
    void shouldHaveDeadValue() {
        assertEquals("DEAD", DeadStoneStatus.DEAD.name());
    }

    @Test
    @DisplayName("should have exactly 2 values")
    void shouldHaveExactlyTwoValues() {
        assertEquals(2, DeadStoneStatus.values().length);
    }

    @Test
    @DisplayName("should return correct value from string")
    void shouldReturnCorrectValueFromString() {
        assertEquals(DeadStoneStatus.ALIVE, DeadStoneStatus.valueOf("ALIVE"));
        assertEquals(DeadStoneStatus.DEAD, DeadStoneStatus.valueOf("DEAD"));
    }

    @Test
    @DisplayName("should throw exception for invalid value")
    void shouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            DeadStoneStatus.valueOf("INVALID");
        });
    }
}
