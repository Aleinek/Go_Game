package student.pwr.dto.websocket;

import java.util.UUID;

/**
 * Payload for GAME_STARTED event.
 * <p>
 * Sent when matchmaking completes and the game begins.
 * </p>
 * 
 * @param gameId the new game's UUID
 * @param yourColor assigned color ("BLACK" or "WHITE")
 * @param opponent information about the opponent
 * @param boardSize the game's board size
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record GameStartedPayload(
    UUID gameId,
    String yourColor,
    OpponentInfo opponent,
    int boardSize
) {
    public record OpponentInfo(String nickname) {}
}
