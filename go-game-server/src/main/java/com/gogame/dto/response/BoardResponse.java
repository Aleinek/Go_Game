package com.gogame.dto.response;

import java.util.List;
import java.util.UUID;

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
