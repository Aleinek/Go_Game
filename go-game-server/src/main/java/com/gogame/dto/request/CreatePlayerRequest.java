package com.gogame.dto.request;

/**
 * Request DTO for creating a new player.
 * <p>
 * Nickname requirements:
 * <ul>
 *   <li>Cannot be empty or null</li>
 *   <li>Must be between 3 and 20 characters</li>
 *   <li>Can only contain letters, numbers, and underscores</li>
 * </ul>
 * </p>
 * 
 * @param nickname the desired player nickname
 * @author Go Game Team
 * @version 1.0
 */
public record CreatePlayerRequest(
    String nickname
) {
    public CreatePlayerRequest {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("Nickname cannot be empty");
        }
        if (nickname.length() < 3 || nickname.length() > 20) {
            throw new IllegalArgumentException("Nickname must be between 3 and 20 characters");
        }
        if (!nickname.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Nickname can only contain letters, numbers and underscores");
        }
    }
}
