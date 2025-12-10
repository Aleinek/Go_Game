package com.gogame.demo;

import java.util.UUID;

public class Player {
    UUID id;
    String nickname;
    int capturedStones;

    public Player(UUID id, String nickname) {
        this.id = id;
        this.nickname = nickname;
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
}
