package student.pwr.dto;

import java.util.UUID;

/**
 * DTO for player registration response.
 * <p>
 * Contains credentials needed for authentication.
 * </p>
 * 
 * @param id the player's unique identifier
 * @param nickname the player's display name
 * @param token authentication token for API requests
 * @param createdAt when the player was created
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record PlayerResponse(UUID id, String nickname, String token, String createdAt) {}