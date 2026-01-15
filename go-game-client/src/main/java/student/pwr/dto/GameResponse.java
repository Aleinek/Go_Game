package student.pwr.dto;

import java.util.UUID;

/**
 * DTO for game state response from the server.
 * <p>
 * Contains game status, player information, and the last move.
 * </p>
 * 
 * @param id the game's unique identifier
 * @param status game status (WAITING, IN_PROGRESS, NEGOTIATING, FINISHED, RESIGNED)
 * @param boardSize the board size (9, 13, or 19)
 * @param currentTurn whose turn it is (BLACK/WHITE)
 * @param moveCount total number of moves
 * @param blackPlayer black player information
 * @param whitePlayer white player information
 * @param message optional status message
 * @param lastMove the most recent move
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record GameResponse(
    UUID id, 
    String status, 
    int boardSize, 
    String currentTurn, 
    Integer moveCount,
    GamePlayer blackPlayer, 
    GamePlayer whitePlayer, 
    String message,
    GameMove lastMove
) {}