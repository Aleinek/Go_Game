package com.gogame.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO returned after creating a player.
 * <p>
 * Contains the player's credentials needed for authentication:
 * <ul>
 *   <li>Player ID (UUID) - used for API calls</li>
 *   <li>Token - used in X-Player-Token header</li>
 * </ul>
 * </p>
 * 
 * @param id the player's unique identifier
 * @param nickname the player's display name
 * @param token authentication token for API requests
 * @param createdAt when the player was created
 * @author Go Game Team
 * @version 1.0
 */
public record PlayerResponse(
    UUID id,
    String nickname,
    String token,
    Instant createdAt
) {
}
