package student.pwr.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for game replay data from server.
 */
public record GameReplayDTO(
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
    public record PlayerInfo(
        UUID playerId,
        String nickname,
        String color,
        int finalCapturedStones
    ) {}

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
