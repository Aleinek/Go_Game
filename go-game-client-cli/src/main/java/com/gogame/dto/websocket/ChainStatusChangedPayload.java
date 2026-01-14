package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia CHAIN_STATUS_CHANGED.
 * Wysyłany gdy gracz zmieni status łańcucha (ALIVE <-> DEAD).
 */
public record ChainStatusChangedPayload(
    int chainId,
    String newStatus,       // "ALIVE" lub "DEAD"
    String changedBy,       // "BLACK" lub "WHITE"
    boolean blackAccepted,
    boolean whiteAccepted
) {}
