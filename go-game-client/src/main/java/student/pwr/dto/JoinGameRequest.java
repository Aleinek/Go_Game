package student.pwr.dto;

/**
 * DTO for joining/creating a game request.
 * 
 * @param boardSize the desired board size (9, 13, or 19)
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record JoinGameRequest(int boardSize) {}