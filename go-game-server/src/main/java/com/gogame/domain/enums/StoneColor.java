package com.gogame.domain.enums;

/**
 * Represents the color of a stone or an empty intersection.
 * <p>
 * In Go:
 * <ul>
 *   <li>BLACK always plays first</li>
 *   <li>WHITE receives komi compensation</li>
 *   <li>EMPTY represents unoccupied intersections</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public enum StoneColor {
    /** Black stones (first player). */
    BLACK,
    /** White stones (second player, receives komi). */
    WHITE,
    /** Empty intersection (no stone placed). */
    EMPTY
}
