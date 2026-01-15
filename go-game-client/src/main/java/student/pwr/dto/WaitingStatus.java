package student.pwr.dto;

import java.util.UUID;

/**
 * DTO for matchmaking waiting status.
 * 
 * @param status current status (WAITING, MATCHED)
 * @param gameId game ID if matched
 * @param message status message
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record WaitingStatus(String status, UUID gameId, String message) {}