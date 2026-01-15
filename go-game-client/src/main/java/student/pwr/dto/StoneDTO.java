package student.pwr.dto;

/**
 * DTO representing a stone on the board.
 * 
 * @param x the x-coordinate (column)
 * @param y the y-coordinate (row)
 * @param color the stone color (BLACK/WHITE)
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record StoneDTO(int x, int y, String color) {}