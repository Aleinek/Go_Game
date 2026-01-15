package com.gogame.dto.request;

/**
 * Request DTO for making a move (placing a stone).
 * <p>
 * Coordinates are 0-based where (0,0) is the top-left corner.
 * </p>
 * 
 * @param x the x-coordinate (column) for the move
 * @param y the y-coordinate (row) for the move
 * @author Go Game Team
 * @version 1.0
 */
public record MakeMoveRequest(
    int x,
    int y
) {
}
