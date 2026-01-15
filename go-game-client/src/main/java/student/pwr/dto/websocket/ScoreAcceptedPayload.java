package student.pwr.dto.websocket;

/**
 * Payload for SCORE_ACCEPTED event.
 * <p>
 * Sent when a player accepts the current score.
 * When both accept, the game ends.
 * </p>
 * 
 * @param acceptedBy "BLACK" or "WHITE"
 * @param blackAccepted whether black has accepted
 * @param whiteAccepted whether white has accepted
 * @param currentScore current score preview
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record ScoreAcceptedPayload(
    String acceptedBy,
    boolean blackAccepted,
    boolean whiteAccepted,
    ScoreBreakdown currentScore
) {
    /**
     * Current score preview breakdown.
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
