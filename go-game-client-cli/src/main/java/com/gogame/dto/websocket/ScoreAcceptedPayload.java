package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia SCORE_ACCEPTED.
 * Wysyłany gdy gracz zaakceptuje aktualny stan punktacji.
 */
public record ScoreAcceptedPayload(
    String acceptedBy,      // "BLACK" lub "WHITE"
    boolean blackAccepted,
    boolean whiteAccepted,
    ScoreBreakdown currentScore
) {
    /**
     * Aktualny podgląd punktacji.
     */
    public record ScoreBreakdown(
        int blackTerritory,
        int whiteTerritory,
        int blackPrisoners,
        int whitePrisoners,
        int blackDeadStones,
        int whiteDeadStones,
        double komi,
        double blackTotal,
        double whiteTotal,
        String winner,
        double scoreDifference
    ) {}
}
