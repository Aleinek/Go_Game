package com.gogame.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for game history listing.
 * 
 * @param games list of saved games
 * @param totalCount total number of games
 */
public record GameHistoryResponse(
    List<GameSummary> games,
    int totalCount
) {
    /**
     * Summary of a single saved game.
     */
    public record GameSummary(
        UUID gameId,
        int boardSize,
        String status,
        PlayerInfo blackPlayer,
        PlayerInfo whitePlayer,
        String winner,
        Double blackScore,
        Double whiteScore,
        int totalMoves,
        Instant createdAt,
        Instant finishedAt
    ) {}

    /**
     * Player info in game summary.
     */
    public record PlayerInfo(
        UUID playerId,
        String nickname,
        int capturedStones
    ) {}
}
