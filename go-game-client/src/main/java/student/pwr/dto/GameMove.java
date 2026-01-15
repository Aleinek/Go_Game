package student.pwr.dto;

/**
 * DTO representing a move in the game.
 * 
 * @param x the x-coordinate (column)
 * @param y the y-coordinate (row)
 * @param color the stone color (BLACK/WHITE)
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record GameMove(int x, int y, String color) {}