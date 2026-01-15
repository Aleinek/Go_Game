package student.pwr.dto;

/**
 * DTO for move response from the server.
 * 
 * @param success whether the move was successful
 * @param message status message
 * @param error error message if move failed
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record MoveResponse(boolean success, String message, String error) {}