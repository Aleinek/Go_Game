package com.gogame.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a position (intersection) on the Go board.
 * <p>
 * Positions use 0-based coordinates where (0,0) is the top-left corner.
 * The x-axis increases rightward, y-axis increases downward.
 * </p>
 * <p>
 * This class is immutable and properly implements {@code equals()} and 
 * {@code hashCode()} for use in collections.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class Position {
    /** The x-coordinate (column) on the board. */
    private final int x;
    /** The y-coordinate (row) on the board. */
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<Position> getNeighbors() {
        List<Position> neighbors = new ArrayList<>();
        neighbors.add(new Position(x + 1, y));
        neighbors.add(new Position(x - 1, y));
        neighbors.add(new Position(x, y + 1));
        neighbors.add(new Position(x, y - 1));
        return neighbors;
    }

    public boolean isValid(int boardSize) {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Position)) {
            return false;
        }
        Position other = (Position) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
}
