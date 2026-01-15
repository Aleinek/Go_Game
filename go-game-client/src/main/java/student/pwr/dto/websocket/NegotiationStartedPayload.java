package student.pwr.dto.websocket;

import java.util.List;

/**
 * Payload for NEGOTIATION_STARTED event.
 * <p>
 * Sent when negotiation (scoring) phase begins after two consecutive passes.
 * </p>
 * 
 * @param chains list of all chains with suggested status
 * @param komi the komi value for scoring
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record NegotiationStartedPayload(
    List<ChainSuggestion> chains,
    double komi
) {
    /**
     * Information about a chain during negotiation.
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
