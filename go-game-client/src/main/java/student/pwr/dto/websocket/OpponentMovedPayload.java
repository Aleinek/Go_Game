package student.pwr.dto.websocket;

import java.util.List;

/**
 * Payload for OPPONENT_MOVED event.
 * <p>
 * Sent when the opponent places a stone.
 * </p>
 * 
 * @param move details about the move
 * @param capturedPositions positions where stones were captured
 * @param currentTurn whose turn it is now
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record OpponentMovedPayload(
    MoveInfo move,
    List<PositionInfo> capturedPositions,
    String currentTurn
) {
    public record MoveInfo(int moveNumber, String color) {}
    public record PositionInfo(int x, int y) {}
}
