package com.gogame.dto.websocket;

import java.time.Instant;

/**
 * Wrapper dla zdarzeń WebSocket przychodzących z serwera.
 * Odpowiada strukturze com.gogame.websocket.GameEvent na serwerze.
 */
public record GameEvent(
    String type,
    Object payload,
    Instant timestamp
) {
    public static final String GAME_STARTED = "GAME_STARTED";
    public static final String OPPONENT_MOVED = "OPPONENT_MOVED";
    public static final String OPPONENT_PASSED = "OPPONENT_PASSED";
    public static final String GAME_ENDED = "GAME_ENDED";
    
    // Negotiation phase events
    public static final String NEGOTIATION_STARTED = "NEGOTIATION_STARTED";
    public static final String CHAIN_STATUS_CHANGED = "CHAIN_STATUS_CHANGED";
    public static final String SCORE_ACCEPTED = "SCORE_ACCEPTED";
    public static final String GAME_RESUMED = "GAME_RESUMED";
    public static final String NEGOTIATION_ERROR = "NEGOTIATION_ERROR";
}
