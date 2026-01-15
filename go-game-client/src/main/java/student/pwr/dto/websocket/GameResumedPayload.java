package student.pwr.dto.websocket;

/**
 * Payload for GAME_RESUMED event.
 * <p>
 * Sent when the game returns from negotiation phase to normal play.
 * </p>
 * 
 * @param resumedBy nickname of player who resumed the game
 * @param currentTurn "BLACK" or "WHITE" - whose turn it is
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record GameResumedPayload(
    String resumedBy,
    String currentTurn
) {}
