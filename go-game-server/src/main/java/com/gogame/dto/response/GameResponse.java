package com.gogame.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO containing game state information.
 * <p>
 * Provides a complete view of a game including:
 * <ul>
 *   <li>Game status and turn information</li>
 *   <li>Both players' information</li>
 *   <li>Move count and last move details</li>
 *   <li>Timestamps</li>
 * </ul>
 * </p>
 * 
 * @param id the game's unique identifier
 * @param status current game status
 * @param boardSize the board size (9, 13, or 19)
 * @param currentTurn whose turn it is (BLACK/WHITE)
 * @param blackPlayer black player information
 * @param whitePlayer white player information
 * @param moveCount total number of moves made
 * @param lastMove details of the most recent move
 * @param createdAt when the game was created
 * @param updatedAt when the game was last updated
 * @param message optional status message
 * @author Go Game Team
 * @version 1.0
 */
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
