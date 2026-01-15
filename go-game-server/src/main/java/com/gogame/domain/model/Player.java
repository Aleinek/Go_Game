package com.gogame.domain.model;

import java.util.UUID;

import com.gogame.domain.enums.StoneColor;

/**
 * Represents a player in a Go game.
 * <p>
 * A player has:
 * <ul>
 *   <li>Unique ID (UUID) used for authentication and WebSocket routing</li>
 *   <li>Nickname displayed in the UI</li>
 *   <li>Assigned stone color (BLACK or WHITE)</li>
 *   <li>Count of captured opponent stones (prisoners)</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class Player {
    /** Unique identifier for the player. */
    UUID id;
    /** Display name for the player. */
    String nickname;
    /** Number of opponent stones captured by this player. */
    int capturedStones;
    /** The stone color assigned to this player (BLACK or WHITE). */
    StoneColor stoneColor;

    public Player(UUID id, String nickname, StoneColor stoneColor) {
        this.id = id;
        this.nickname = nickname;
        this.stoneColor = stoneColor;
        this.capturedStones = 0;
    }

    public UUID getId() {
        return id;
    }
    
    public String getNickname() {
        return nickname;
    }

    public void addCaptured(int count) {
        this.capturedStones += count;
    }

    public int getCapturedStones() {
        return capturedStones;
    }

    public StoneColor getStoneColor() {
        return stoneColor;
    }
}
