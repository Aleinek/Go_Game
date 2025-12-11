package com.gogame.websocket;

import java.time.Instant;

public record GameEvent(
    String type,
    Object payload,
    Instant timestamp
) {
    public GameEvent(String type, Object payload) {
        this(type, payload, Instant.now());
    }
    
    public static final String GAME_STARTED = "GAME_STARTED";
    public static final String OPPONENT_MOVED = "OPPONENT_MOVED";
    public static final String OPPONENT_PASSED = "OPPONENT_PASSED";
    public static final String GAME_ENDED = "GAME_ENDED";
}
