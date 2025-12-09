package com.gogame.demo;

import java.util.ArrayList;
import java.util.List;

public class Board {
    int size;
    private Stone[][] grid;
    List<Chain> chains;
    
    public Board(int size) {
        this.size = size;
        this.chains = new ArrayList<>();
        this.grid = new Stone[size][size];
    }

    public void placeStone(Position position, StoneColor color) {
        // if (!isValidMove(position, color)) {
        //     throw new InvalidMoveException(ErrorCode.POSITION_OCCUPIED, position);
        // }
        Stone stone = new Stone(position, color);
        grid[position.getX()][position.getY()] = stone;
    }

    public int getSize() {
        return size;
    }

    public Stone[][] getGrid() {
        return grid;
    }
}