package com.gogame.dto.response;

import java.time.Instant;
import java.util.List;

public record MoveResponse(
    boolean success,
    MoveInfo move,
    List<PositionInfo> capturedPositions,
    String currentTurn,
    BoardInfo board,
    String message
) {
    public record MoveInfo(
        int moveNumber,
        int x,
        int y,
        String color,
        int capturedStones,
        Instant timestamp
    ) {}
    
    public record PositionInfo(
        int x,
        int y
    ) {}
    
    public record BoardInfo(
        int size,
        List<StoneInfo> stones
    ) {}
    
    public record StoneInfo(
        int x,
        int y,
        String color
    ) {}
}
