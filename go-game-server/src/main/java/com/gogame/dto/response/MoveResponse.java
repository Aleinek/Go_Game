package com.gogame.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Response DTO returned after making a move.
 * <p>
 * Contains:
 * <ul>
 *   <li>Success indicator</li>
 *   <li>Move details</li>
 *   <li>Any captured stones</li>
 *   <li>Updated board state</li>
 * </ul>
 * </p>
 * 
 * @param success whether the move was successful
 * @param move details of the move made
 * @param capturedPositions positions where stones were captured
 * @param currentTurn whose turn it is now
 * @param board current board state
 * @param message optional status message
 * @author Go Game Team
 * @version 1.0
 */
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
