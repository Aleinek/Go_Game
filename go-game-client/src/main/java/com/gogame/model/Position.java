package com.gogame.model;

import java.util.ArrayList;
import java.util.List;

public class Position {
    private final int x;
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
        // TODO [] tutaj warto by sprawdzic czy jest valid przed dodaniem do tablicy
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
