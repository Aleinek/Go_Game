package com.gogame.dto.request;

/**
 * Request DTO for joining or creating a game.
 * <p>
 * When a player joins, matchmaking finds another player waiting
 * with the same board size preference.
 * </p>
 * 
 * @param boardSize the desired board size (9, 13, or 19)
 * @author Go Game Team
 * @version 1.0
 */
public record JoinGameRequest(
    int boardSize
) {
    public JoinGameRequest {
        if (boardSize != 9 && boardSize != 13 && boardSize != 19) {
            throw new IllegalArgumentException("Board size must be 9, 13, or 19");
        }
    }
}
