package com.gogame.model;

import java.util.UUID;

/**
 * Exception thrown when a player with the specified ID or nickname cannot be found.
 */
public class PlayerNotFoundException extends RuntimeException {

    private final UUID playerId;
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
