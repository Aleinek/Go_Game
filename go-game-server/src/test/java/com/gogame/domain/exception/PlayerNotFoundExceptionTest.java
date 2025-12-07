package com.gogame.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerNotFoundException Tests")
class PlayerNotFoundExceptionTest {

    @Nested
    @DisplayName("Constructor with UUID")
    class ConstructorWithUuidTests {

        @Test
        @DisplayName("should create exception with player UUID")
        void shouldCreateExceptionWithPlayerUuid() {
            UUID playerId = UUID.randomUUID();
            PlayerNotFoundException exception = new PlayerNotFoundException(playerId);
            
            assertEquals(playerId, exception.getPlayerId());
            assertNull(exception.getNickname());
            assertTrue(exception.getMessage().contains(playerId.toString()));
        }

        @Test
        @DisplayName("should include 'not found' in message")
        void shouldIncludeNotFoundInMessage() {
            UUID playerId = UUID.randomUUID();
            PlayerNotFoundException exception = new PlayerNotFoundException(playerId);
            
            assertTrue(exception.getMessage().toLowerCase().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Constructor with Nickname")
    class ConstructorWithNicknameTests {

        @Test
        @DisplayName("should create exception with nickname")
        void shouldCreateExceptionWithNickname() {
            String nickname = "GoMaster2025";
            PlayerNotFoundException exception = new PlayerNotFoundException(nickname);
            
            assertEquals(nickname, exception.getNickname());
            assertNull(exception.getPlayerId());
            assertTrue(exception.getMessage().contains(nickname));
        }

        @Test
        @DisplayName("should include 'not found' in nickname message")
        void shouldIncludeNotFoundInNicknameMessage() {
            String nickname = "Player123";
            PlayerNotFoundException exception = new PlayerNotFoundException(nickname);
            
            assertTrue(exception.getMessage().toLowerCase().contains("not found"));
        }

        @Test
        @DisplayName("should handle empty nickname")
        void shouldHandleEmptyNickname() {
            PlayerNotFoundException exception = new PlayerNotFoundException("");
            
            assertEquals("", exception.getNickname());
            assertNull(exception.getPlayerId());
        }
    }

    @Nested
    @DisplayName("Constructor with Cause")
    class ConstructorWithCauseTests {

        @Test
        @DisplayName("should create exception with UUID and cause")
        void shouldCreateExceptionWithUuidAndCause() {
            UUID playerId = UUID.randomUUID();
            Exception cause = new RuntimeException("Database connection failed");
            PlayerNotFoundException exception = new PlayerNotFoundException(playerId, cause);
            
            assertEquals(playerId, exception.getPlayerId());
            assertEquals(cause, exception.getCause());
            assertNull(exception.getNickname());
        }
    }

    @Nested
    @DisplayName("Exception Behavior")
    class ExceptionBehaviorTests {

        @Test
        @DisplayName("should be throwable")
        void shouldBeThrowable() {
            assertThrows(PlayerNotFoundException.class, () -> {
                throw new PlayerNotFoundException(UUID.randomUUID());
            });
        }

        @Test
        @DisplayName("should be RuntimeException")
        void shouldBeRuntimeException() {
            PlayerNotFoundException exception = new PlayerNotFoundException(UUID.randomUUID());
            assertTrue(exception instanceof RuntimeException);
        }

        @Test
        @DisplayName("UUID and nickname constructors should produce different state")
        void uuidAndNicknameConstructorsShouldProduceDifferentState() {
            UUID playerId = UUID.randomUUID();
            String nickname = "TestPlayer";
            
            PlayerNotFoundException byUuid = new PlayerNotFoundException(playerId);
            PlayerNotFoundException byNickname = new PlayerNotFoundException(nickname);
            
            assertNotNull(byUuid.getPlayerId());
            assertNull(byUuid.getNickname());
            
            assertNull(byNickname.getPlayerId());
            assertNotNull(byNickname.getNickname());
        }

        @Test
        @DisplayName("should be catchable as Exception")
        void shouldBeCatchableAsException() {
            try {
                throw new PlayerNotFoundException("TestUser");
            } catch (PlayerNotFoundException e) {
                assertTrue(true);
            } catch (Exception e) {
                fail("Should catch PlayerNotFoundException");
            }
        }
    }
}
