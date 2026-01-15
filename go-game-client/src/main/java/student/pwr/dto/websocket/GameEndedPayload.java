package student.pwr.dto.websocket;

/**
 * Payload for GAME_ENDED event.
 * <p>
 * Sent when the game ends (resignation or scoring complete).
 * </p>
 * 
 * @param reason "RESIGNATION" or "SCORING_COMPLETE"
 * @param winner "BLACK" or "WHITE"
 * @param resignedBy nickname of resigning player (null if scoring)
 * @param score detailed score breakdown (null if resignation)
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record GameEndedPayload(
    String reason,
    String winner,
    String resignedBy,
    ScoreBreakdown score
) {
    /**
     * Detailed score breakdown using Japanese rules.
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
