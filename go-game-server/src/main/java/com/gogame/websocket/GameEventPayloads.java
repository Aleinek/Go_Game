package com.gogame.websocket;

import java.util.List;
import java.util.UUID;

public class GameEventPayloads {
    
    public record GameStartedPayload(
        UUID gameId,
        String yourColor,
        OpponentInfo opponent,
        int boardSize
    ) {}
    
    public record OpponentInfo(
        String nickname
    ) {}
    
    public record OpponentMovedPayload(
        MoveInfo move,
        List<PositionInfo> capturedPositions,
        String currentTurn
    ) {}
    
    public record MoveInfo(
        int moveNumber,
        int x,
        int y,
        String color
    ) {}
    
    public record PositionInfo(
        int x,
        int y
    ) {}
    
    public record OpponentPassedPayload(
        int moveNumber,
        int consecutivePasses,
        String currentTurn
    ) {}
    
    public record GameEndedPayload(
        String reason,
        String winner,
        String resignedBy
    ) {}
}
