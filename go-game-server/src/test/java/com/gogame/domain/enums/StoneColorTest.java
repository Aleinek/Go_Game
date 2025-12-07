package com.gogame.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StoneColor Enum Tests")
class StoneColorTest {

    @Test
    @DisplayName("should have BLACK value")
    void shouldHaveBlackValue() {
        assertEquals("BLACK", StoneColor.BLACK.name());
    }

    @Test
    @DisplayName("should have WHITE value")
    void shouldHaveWhiteValue() {
        assertEquals("WHITE", StoneColor.WHITE.name());
    }

    @Test
    @DisplayName("should have EMPTY value")
    void shouldHaveEmptyValue() {
        assertEquals("EMPTY", StoneColor.EMPTY.name());
    }

    @Test
    @DisplayName("should have exactly 3 values")
    void shouldHaveExactlyThreeValues() {
        assertEquals(3, StoneColor.values().length);
    }

    @Test
    @DisplayName("should return correct value from string")
    void shouldReturnCorrectValueFromString() {
        assertEquals(StoneColor.BLACK, StoneColor.valueOf("BLACK"));
        assertEquals(StoneColor.WHITE, StoneColor.valueOf("WHITE"));
        assertEquals(StoneColor.EMPTY, StoneColor.valueOf("EMPTY"));
    }

    @Test
    @DisplayName("should throw exception for invalid value")
    void shouldThrowExceptionForInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> {
            StoneColor.valueOf("INVALID");
        });
    }

    @Test
    @DisplayName("BLACK and WHITE should be different")
    void blackAndWhiteShouldBeDifferent() {
        assertNotEquals(StoneColor.BLACK, StoneColor.WHITE);
    }

    @Test
    @DisplayName("EMPTY should be different from player colors")
    void emptyShouldBeDifferentFromPlayerColors() {
        assertNotEquals(StoneColor.EMPTY, StoneColor.BLACK);
        assertNotEquals(StoneColor.EMPTY, StoneColor.WHITE);
    }
}
