package com.gogame.domain.exception;
import java.util.UUID;

/**
 * Exception thrown when a game with the specified ID cannot be found.
 * <p>
 * This typically occurs when:
 * <ul>
 *   <li>A player tries to access a game that has ended</li>
 *   <li>An invalid game ID is provided in the request</li>
 *   <li>The game was never created</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class GameNotFoundException extends RuntimeException {
    /** The game ID that was not found. */
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
