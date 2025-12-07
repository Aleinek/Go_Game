package com.gogame.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameNotFoundException Tests")
class GameNotFoundExceptionTest {

    @Nested
    @DisplayName("Constructor with UUID")
    class ConstructorWithUuidTests {

        @Test
        @DisplayName("should create exception with UUID")
        void shouldCreateExceptionWithUuid() {
            UUID gameId = UUID.randomUUID();
            GameNotFoundException exception = new GameNotFoundException(gameId);
            
            assertEquals(gameId, exception.getGameId());
            assertTrue(exception.getMessage().contains(gameId.toString()));
        }

        @Test
        @DisplayName("should include 'not found' in message")
        void shouldIncludeNotFoundInMessage() {
            UUID gameId = UUID.randomUUID();
            GameNotFoundException exception = new GameNotFoundException(gameId);
            
            assertTrue(exception.getMessage().toLowerCase().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Constructor with String")
    class ConstructorWithStringTests {

        @Test
        @DisplayName("should create exception with valid UUID string")
        void shouldCreateExceptionWithValidUuidString() {
            String gameIdString = "550e8400-e29b-41d4-a716-446655440000";
            GameNotFoundException exception = new GameNotFoundException(gameIdString);
            
            assertEquals(UUID.fromString(gameIdString), exception.getGameId());
            assertTrue(exception.getMessage().contains(gameIdString));
        }

        @Test
        @DisplayName("should handle invalid UUID string gracefully")
        void shouldHandleInvalidUuidStringGracefully() {
            String invalidId = "not-a-valid-uuid";
            GameNotFoundException exception = new GameNotFoundException(invalidId);
            
            assertNull(exception.getGameId());
            assertTrue(exception.getMessage().contains(invalidId));
        }
    }

    @Nested
    @DisplayName("Constructor with Cause")
    class ConstructorWithCauseTests {

        @Test
        @DisplayName("should create exception with UUID and cause")
        void shouldCreateExceptionWithUuidAndCause() {
            UUID gameId = UUID.randomUUID();
            Exception cause = new RuntimeException("Database error");
            GameNotFoundException exception = new GameNotFoundException(gameId, cause);
            
            assertEquals(gameId, exception.getGameId());
            assertEquals(cause, exception.getCause());
        }
    }

    @Nested
    @DisplayName("Exception Behavior")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("should be throwable")
        void shouldBeThrowable() {
            assertThrows(GameNotFoundException.class, () -> {
                throw new GameNotFoundException(UUID.randomUUID());
            });
        }

        @Test
        @DisplayName("should be RuntimeException")
        void shouldBeRuntimeException() {
            GameNotFoundException exception = new GameNotFoundException(UUID.randomUUID());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("should be catchable as Exception")
        void shouldBeCatchableAsException() {
            try {
                throw new GameNotFoundException(UUID.randomUUID());
            } catch (GameNotFoundException e) {
                assertTrue(true);
            } catch (Exception e) {
                fail("Should catch GameNotFoundException");
            }
        }
    }
}
