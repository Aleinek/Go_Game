package com.gogame.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
