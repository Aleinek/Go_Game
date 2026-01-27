package com.gogame.repository.entity;

import java.util.UUID;

/**
 * Embedded document representing a player in a saved game.
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class PlayerDocument {
    
    private UUID playerId;
    private String nickname;
    private String stoneColor;
    private int capturedStones;

    public PlayerDocument() {}

    public PlayerDocument(UUID playerId, String nickname, String stoneColor, int capturedStones) {
        this.playerId = playerId;
        this.nickname = nickname;
        this.stoneColor = stoneColor;
        this.capturedStones = capturedStones;
    }

    // Getters and Setters
    public UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getStoneColor() {
        return stoneColor;
    }

    public void setStoneColor(String stoneColor) {
        this.stoneColor = stoneColor;
    }

    public int getCapturedStones() {
        return capturedStones;
    }

    public void setCapturedStones(int capturedStones) {
        this.capturedStones = capturedStones;
    }
}
