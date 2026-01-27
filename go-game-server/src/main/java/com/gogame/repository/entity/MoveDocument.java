package com.gogame.repository.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * Embedded document representing a single move in a saved game.
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class MoveDocument {
    
    private UUID moveId;
    private int moveNumber;
    private UUID playerId;
    private String playerColor;
    private Integer x;
    private Integer y;
    private boolean isPass;
    private int capturedStones;
    private Instant timestamp;

    public MoveDocument() {}

    public MoveDocument(UUID moveId, int moveNumber, UUID playerId, String playerColor,
                        Integer x, Integer y, boolean isPass, int capturedStones, Instant timestamp) {
        this.moveId = moveId;
        this.moveNumber = moveNumber;
        this.playerId = playerId;
        this.playerColor = playerColor;
        this.x = x;
        this.y = y;
        this.isPass = isPass;
        this.capturedStones = capturedStones;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public UUID getMoveId() {
        return moveId;
    }

    public void setMoveId(UUID moveId) {
        this.moveId = moveId;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public boolean isPass() {
        return isPass;
    }

    public void setPass(boolean pass) {
        isPass = pass;
    }

    public int getCapturedStones() {
        return capturedStones;
    }

    public void setCapturedStones(int capturedStones) {
        this.capturedStones = capturedStones;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
