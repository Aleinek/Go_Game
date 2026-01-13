package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia GAME_ENDED.
 * Wysyłany gdy gra się kończy (rezygnacja lub dwa pasy pod rząd).
 */
public record GameEndedPayload(
    String reason,      // "RESIGNATION" lub "TWO_PASSES"
    String winner,      // nick zwycięzcy
    String resignedBy   // nick rezygnującego (tylko gdy reason == RESIGNATION)
) {}
