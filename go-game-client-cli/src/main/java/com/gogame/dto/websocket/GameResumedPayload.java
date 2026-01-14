package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia GAME_RESUMED.
 * Wysyłany gdy gra wraca z fazy negocjacji do normalnej rozgrywki.
 */
public record GameResumedPayload(
    String resumedBy,       // nick gracza który wznowił grę
    String currentTurn      // "BLACK" lub "WHITE" - kto ma teraz kolejkę
) {}
