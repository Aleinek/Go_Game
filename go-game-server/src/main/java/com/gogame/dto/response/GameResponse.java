package com.gogame.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GameResponse(
    UUID id,
    String status,
    int boardSize,
    String currentTurn,
    PlayerInfo blackPlayer,
    PlayerInfo whitePlayer,
    int moveCount,
    MoveInfo lastMove,
    Instant createdAt,
    Instant updatedAt,
    String message
) {
    public record PlayerInfo(
        UUID id,
        String nickname,
        int capturedStones
    ) {}
    
    public record MoveInfo(
        int moveNumber,
        Integer x,
        Integer y,
        String color,
        int capturedStones,
        Instant timestamp
    ) {}
}
