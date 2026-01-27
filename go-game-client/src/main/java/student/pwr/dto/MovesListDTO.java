package student.pwr.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for moves list response from server.
 */
public record MovesListDTO(
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
