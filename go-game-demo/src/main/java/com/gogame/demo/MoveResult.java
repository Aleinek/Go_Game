package com.gogame.demo;

import java.util.List;

public class MoveResult {
    boolean success;
    String errorMessage;
    List<Stone> capturedStones;
    boolean gameEnded;

    public MoveResult(boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }

    public List<Stone> getCapturedStones() {
        return capturedStones;
    }
}
