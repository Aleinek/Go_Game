package com.gogame.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a player with the specified ID or nickname cannot be found.
 * <p>
 * This can occur when:
 * <ul>
 *   <li>An invalid player ID is used for authentication</li>
 *   <li>A player is looked up by nickname that doesn't exist</li>
 *   <li>A player has disconnected or their session expired</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class PlayerNotFoundException extends RuntimeException {
    /** The player ID that was not found. */
    private final UUID playerId;
    /** The nickname that was not found. */
    private final String nickname;

    public PlayerNotFoundException(UUID playerId) {
        super(String.format("Player with ID '%s' not found", playerId));
        this.playerId = playerId;
        this.nickname = null;
    }

    public PlayerNotFoundException(String nickname) {
        super(String.format("Player with nickname '%s' not found", nickname));
        this.playerId = null;
        this.nickname = nickname;
    }

    public PlayerNotFoundException(UUID playerId, Throwable cause) {
        super(String.format("Player with ID '%s' not found", playerId), cause);
        this.playerId = playerId;
        this.nickname = null;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getNickname() {
        return nickname;
    }
}
