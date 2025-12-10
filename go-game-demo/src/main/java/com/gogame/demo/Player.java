package com.gogame.demo;

import java.util.UUID;

public class Player {
    UUID id;
    String nickname;
    int capturedStones;
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
