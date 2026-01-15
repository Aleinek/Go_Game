package student.pwr.dto;

/**
 * DTO for making a move (placing a stone).
 * 
 * @param x the x-coordinate (column)
 * @param y the y-coordinate (row)
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record MakeMoveRequest(int x, int y) {}