package com.gogame.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for game replay data.
 * Contains all information needed to replay a game step by step.
 * 
 * @param gameId the game's unique identifier
 * @param boardSize the board size (9, 13, or 19)
 * @param blackPlayer black player info
 * @param whitePlayer white player info
 * @param moves list of all moves in order
 * @param finalStatus the game's final status
 * @param winner the winner (BLACK, WHITE, or null for draw)
 * @param blackScore black's final score
 * @param whiteScore white's final score
 * @param komi the komi value used
 */
public record GameReplayResponse(
    UUID gameId,
    int boardSize,
    PlayerInfo blackPlayer,
    PlayerInfo whitePlayer,
    List<ReplayMove> moves,
    String finalStatus,
    String winner,
    Double blackScore,
    Double whiteScore,
    double komi,
    Instant createdAt,
    Instant finishedAt
) {
    /**
     * Player information for replay.
     */
    public record PlayerInfo(
        UUID playerId,
        String nickname,
        String color,
        int finalCapturedStones
    ) {}

    /**
     * Single move for replay purposes.
     */
    public record ReplayMove(
        int moveNumber,
        String playerColor,
        Integer x,
        Integer y,
        boolean isPass,
        int capturedStones,
        Instant timestamp
    ) {}
}
