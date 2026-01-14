package com.gogame.dto.websocket;

/**
 * Payload dla zdarzenia GAME_ENDED.
 * Wysyłany gdy gra się kończy (rezygnacja, dwa pasy lub zakończenie negocjacji).
 */
public record GameEndedPayload(
    String reason,      // "RESIGNATION", "TWO_PASSES", lub "SCORING_COMPLETE"
    String winner,      // nick zwycięzcy lub "BLACK"/"WHITE"
    String resignedBy,  // nick rezygnującego (tylko gdy reason == RESIGNATION)
    ScoreBreakdown score // szczegóły punktacji (tylko gdy reason == SCORING_COMPLETE)
) {
    /**
     * Szczegółowy rozkład punktacji według zasad japońskich.
     */
    public record ScoreBreakdown(
        int blackTerritory,
        int whiteTerritory,
        int blackPrisoners,      // zdobyci podczas gry
        int whitePrisoners,      // zdobyci podczas gry
        int blackDeadStones,     // oznaczone jako martwe w negocjacjach
        int whiteDeadStones,     // oznaczone jako martwe w negocjacjach
        double komi,
        double blackTotal,
        double whiteTotal,
        String winner,
        double scoreDifference
    ) {}
}
