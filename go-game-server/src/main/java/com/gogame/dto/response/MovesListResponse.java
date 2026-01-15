package com.gogame.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing the list of all moves in a game.
 * <p>
 * Provides the complete move history for game replay or analysis.
 * </p>
 * 
 * @param gameId the game's unique identifier
 * @param moves list of all moves made in the game
 * @param message optional status message
 * @author Go Game Team
 * @version 1.0
 */
public record MovesListResponse(
    UUID gameId,
    List<MoveInfo> moves,
    String message
) {
    public record MoveInfo(
        int moveNumber,
        Integer x,
        Integer y,
        String color,
        Instant timestamp
    ) {}
}
