package com.gogame.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing the current state of the game board.
 * <p>
 * Includes all stone positions, captured stone counts, and territory information.
 * </p>
 * 
 * @param gameId the game's unique identifier
 * @param size the board size (9, 13, or 19)
 * @param moveNumber the current move number
 * @param stones list of all stones on the board
 * @param blackCaptured stones captured by black player
 * @param whiteCaptured stones captured by white player
 * @param whiteTerritory white's territory count
 * @param blackTerritory black's territory count
 * @param neutralTerritory neutral points (dame)
 * @author Go Game Team
 * @version 1.0
 */
public record BoardResponse(
    UUID gameId,
    int size,
    int moveNumber,
    List<StoneInfo> stones,
    int blackCaptured,
    int whiteCaptured,
    int whiteTerritory,
    int blackTerritory,
    int neutralTerritory
) {
    public record StoneInfo(
        int x,
        int y,
        String color
    ) {}
}
