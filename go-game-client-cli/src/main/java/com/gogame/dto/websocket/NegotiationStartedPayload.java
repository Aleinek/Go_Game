package com.gogame.dto.websocket;

import java.util.List;

/**
 * Payload dla zdarzenia NEGOTIATION_STARTED.
 * Wysyłany gdy rozpoczyna się faza negocjacji punktacji (po dwóch passach).
 */
public record NegotiationStartedPayload(
    List<ChainSuggestion> chains,
    double komi
) {
    /**
     * Informacja o łańcuchu kamieni podczas negocjacji.
     */
    public record ChainSuggestion(
        int chainId,
        List<PositionInfo> positions,
        String color,       // "BLACK" lub "WHITE"
        int liberties,
        String status       // "ALIVE" lub "DEAD"
    ) {}
    
    public record PositionInfo(
        int x,
        int y
    ) {}
}
