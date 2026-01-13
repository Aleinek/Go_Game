package com.gogame.dto.websocket;

import java.util.UUID;

/**
 * Payload dla zdarzenia GAME_STARTED.
 * Wysyłany gdy matchmaking się zakończy i gra się rozpoczyna.
 */
public record GameStartedPayload(
    UUID gameId,
    String yourColor,
    OpponentInfo opponent,
    int boardSize
) {
    public record OpponentInfo(String nickname) {}
}
