package com.gogame.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a game with the specified ID cannot be found.
 */
public class GameNotFoundException extends RuntimeException {

    private final UUID gameId;

    public GameNotFoundException(UUID gameId) {
        super(String.format("Game with ID '%s' not found", gameId));
        this.gameId = gameId;
    }

    public GameNotFoundException(String gameId) {
        super(String.format("Game with ID '%s' not found", gameId));
        this.gameId = parseUuidSafely(gameId);
    }

    public GameNotFoundException(UUID gameId, Throwable cause) {
        super(String.format("Game with ID '%s' not found", gameId), cause);
        this.gameId = gameId;
    }

    public UUID getGameId() {
        return gameId;
    }

    private static UUID parseUuidSafely(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
