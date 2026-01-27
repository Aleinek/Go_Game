package student.pwr.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for game history response from server.
 */
public record GameHistoryDTO(
    List<GameSummary> games,
    int totalCount
) {
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

    public record PlayerInfo(
        UUID playerId,
        String nickname,
        int capturedStones
    ) {}
}
