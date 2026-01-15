package student.pwr.dto.websocket;

/**
 * Payload for OPPONENT_PASSED event.
 * <p>
 * Sent when the opponent passes their turn.
 * Two consecutive passes trigger negotiation phase.
 * </p>
 * 
 * @param moveNumber the move number of the pass
 * @param consecutivePasses total consecutive passes
 * @param currentTurn whose turn it is now
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record OpponentPassedPayload(
    int moveNumber,
    int consecutivePasses,
    String currentTurn
) {}
