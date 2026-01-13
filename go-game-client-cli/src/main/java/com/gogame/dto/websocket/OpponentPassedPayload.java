package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia OPPONENT_PASSED.
 * Wysyłany gdy przeciwnik spasuje.
 */
public record OpponentPassedPayload(
    int moveNumber,
    int consecutivePasses,
    String currentTurn
) {}
