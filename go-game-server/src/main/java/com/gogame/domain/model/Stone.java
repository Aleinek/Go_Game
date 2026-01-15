package com.gogame.domain.model;

import com.gogame.domain.enums.StoneColor;

/**
 * Represents a single stone on the Go board.
 * <p>
 * A stone is immutable after placement and has:
 * <ul>
 *   <li>A position (x, y coordinates on the board)</li>
 *   <li>A color (BLACK or WHITE)</li>
 * </ul>
 * </p>
 * <p>
 * Stones are grouped into {@link Chain}s when they share orthogonal connections.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class Stone {
    /** The position of this stone on the board. */
    private final Position position;
    /** The color of this stone (BLACK or WHITE). */
    private final StoneColor color;

    public Stone(Position position, StoneColor color) {
        this.position = position;
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public StoneColor getColor() {
        return color;
    }
}
