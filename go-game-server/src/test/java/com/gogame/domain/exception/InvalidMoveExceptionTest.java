package com.gogame.domain.exception;

import com.gogame.domain.model.Position;
import com.gogame.domain.exception.InvalidMoveException.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InvalidMoveException Tests")
class InvalidMoveExceptionTest {

    @Nested
    @DisplayName("ErrorCode Enum")
    class ErrorCodeTests {

        @Test
        @DisplayName("should have POSITION_OCCUPIED error code")
        void shouldHavePositionOccupiedErrorCode() {
            assertEquals("Position is already occupied", ErrorCode.POSITION_OCCUPIED.getDescription());
        }

        @Test
        @DisplayName("should have OUT_OF_BOUNDS error code")
        void shouldHaveOutOfBoundsErrorCode() {
            assertEquals("Position is outside the board boundaries", ErrorCode.OUT_OF_BOUNDS.getDescription());
        }

        @Test
        @DisplayName("should have NOT_YOUR_TURN error code")
        void shouldHaveNotYourTurnErrorCode() {
            assertEquals("It is not your turn to play", ErrorCode.NOT_YOUR_TURN.getDescription());
        }

        @Test
        @DisplayName("should have SUICIDE_MOVE error code")
        void shouldHaveSuicideMoveErrorCode() {
            assertEquals("Move would result in immediate capture (suicide)", ErrorCode.SUICIDE_MOVE.getDescription());
        }

        @Test
        @DisplayName("should have KO_VIOLATION error code")
        void shouldHaveKoViolationErrorCode() {
            assertEquals("Move violates the Ko rule", ErrorCode.KO_VIOLATION.getDescription());
        }

        @Test
        @DisplayName("should have GAME_NOT_IN_PROGRESS error code")
        void shouldHaveGameNotInProgressErrorCode() {
            assertEquals("Game is not in progress", ErrorCode.GAME_NOT_IN_PROGRESS.getDescription());
        }

        @Test
        @DisplayName("should have INVALID_POSITION error code")
        void shouldHaveInvalidPositionErrorCode() {
            assertEquals("Invalid position coordinates", ErrorCode.INVALID_POSITION.getDescription());
        }

        @Test
        @DisplayName("should have exactly 7 error codes")
        void shouldHaveExactlySevenErrorCodes() {
            assertEquals(7, ErrorCode.values().length);
        }
    }

    @Nested
    @DisplayName("Constructor with ErrorCode")
    class ConstructorWithErrorCodeTests {

        @Test
        @DisplayName("should create exception with error code")
        void shouldCreateExceptionWithErrorCode() {
            InvalidMoveException exception = new InvalidMoveException(ErrorCode.POSITION_OCCUPIED);
            
            assertEquals(ErrorCode.POSITION_OCCUPIED, exception.getErrorCode());
            assertEquals("Position is already occupied", exception.getMessage());
            assertNull(exception.getPosition());
        }

        @Test
        @DisplayName("should create exception with error code and position")
        void shouldCreateExceptionWithErrorCodeAndPosition() {
            Position pos = new Position(3, 5);
            InvalidMoveException exception = new InvalidMoveException(ErrorCode.POSITION_OCCUPIED, pos);
            
            assertEquals(ErrorCode.POSITION_OCCUPIED, exception.getErrorCode());
            assertEquals(pos, exception.getPosition());
            assertTrue(exception.getMessage().contains("3"));
            assertTrue(exception.getMessage().contains("5"));
        }
    }

    @Nested
    @DisplayName("Constructor with Message")
    class ConstructorWithMessageTests {

        @Test
        @DisplayName("should create exception with custom message")
        void shouldCreateExceptionWithCustomMessage() {
            InvalidMoveException exception = new InvalidMoveException("Custom error message");
            
            assertEquals("Custom error message", exception.getMessage());
            assertNull(exception.getErrorCode());
            assertNull(exception.getPosition());
        }

        @Test
        @DisplayName("should create exception with message and position")
        void shouldCreateExceptionWithMessageAndPosition() {
            Position pos = new Position(7, 8);
            InvalidMoveException exception = new InvalidMoveException("Custom error", pos);
            
            assertTrue(exception.getMessage().contains("Custom error"));
            assertTrue(exception.getMessage().contains("7"));
            assertTrue(exception.getMessage().contains("8"));
            assertEquals(pos, exception.getPosition());
        }
    }

    @Nested
    @DisplayName("Exception Behavior")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("should be throwable")
        void shouldBeThrowable() {
            assertThrows(InvalidMoveException.class, () -> {
                throw new InvalidMoveException(ErrorCode.NOT_YOUR_TURN);
            });
        }

        @Test
        @DisplayName("should be RuntimeException")
        void shouldBeRuntimeException() {
            InvalidMoveException exception = new InvalidMoveException(ErrorCode.SUICIDE_MOVE);
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("should preserve position in message for all error codes")
        void shouldPreservePositionInMessageForAllErrorCodes() {
            Position pos = new Position(10, 15);
            
            for (ErrorCode code : ErrorCode.values()) {
                InvalidMoveException exception = new InvalidMoveException(code, pos);
                assertTrue(exception.getMessage().contains("10"), 
                    "Message should contain x coordinate for " + code);
                assertTrue(exception.getMessage().contains("15"), 
                    "Message should contain y coordinate for " + code);
            }
        }
    }
}
