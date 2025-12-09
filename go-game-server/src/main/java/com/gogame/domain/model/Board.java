package com.gogame.domain.model;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.exception.InvalidMoveException.ErrorCode;

public class Board {
    private final int size;
    private final Stone[][] grid;
    Map<Position, Chain> chains;

    public Board(int size) {
        this.size = size;
        this.grid = new Stone[size][size];
    }

    public int getSize() {
        return size;
    }

    public Stone getStoneAt(Position position) {
        if (!position.isValid(size)) {
            throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS, position);
        }
        return grid[position.getX()][position.getY()];
    }

    public void placeStone(Position position, StoneColor color) {
        if (!isValidMove(position, color)) {
            throw new InvalidMoveException(ErrorCode.POSITION_OCCUPIED, position);
        }
        Stone stone = new Stone(position, color);
        grid[position.getX()][position.getY()] = stone;
    }

    public boolean isValidMove(Position position, StoneColor color) {
        return position.isValid(size) && this.isEmpty(position);
    }

    public boolean isEmpty(Position position) {
        return getStoneAt(position) == null;
    }

    public Board copy() {
        Board newBoard = new Board(this.size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                newBoard.grid[i][j] = this.grid[i][j];
            }
        }
        return newBoard;
    }

    void updateChain(Position position, Chain chain) {
        chains.put(position, chain);
    }

    List<Stone> checkCaptures(Position position, StoneColor opponentColor) {
        List<Stone> capturedStones = new ArrayList<>();
        for (Position neighbor : position.getNeighbors()) {
            if (neighbor.isValid(size)) {
                Chain neighborChain = chains.get(neighbor);
                if (neighborChain != null && neighborChain.color == opponentColor) {
                    if (neighborChain.isCaptured(this)) {
                        capturedStones.addAll(neighborChain.getStones());
                    }
                }
            }
        }
        return capturedStones;
    }

    Chain findChain(Position position) {
        return chains.get(position);
    }
}
