package com.gogame.dto.websocket;

import java.util.List;

/**
 * Payload dla zdarzenia OPPONENT_MOVED.
 * Wysyłany gdy przeciwnik wykona ruch.
 */
public record OpponentMovedPayload(
    MoveInfo move,
    List<PositionInfo> capturedPositions,
    String currentTurn
) {
    public record MoveInfo(int moveNumber, String color) {}
    public record PositionInfo(int x, int y) {}
}
