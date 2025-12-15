package com.gogame.model;

import java.util.List;

import com.gogame.APIController;
import com.gogame.dto.BoardResponseDTO;
import com.gogame.dto.StoneDTO;

public class Board {

    int size;
    Stone[][] stones;
    Territory territory;

    public Board(BoardResponseDTO boardResponseDTO) {
        this.size = boardResponseDTO.size();
        this.stones = new Stone[size][size];
        List<StoneDTO> stonesList = boardResponseDTO.stones();
        for (StoneDTO stoneDTO : stonesList) {
            int x = stoneDTO.x();
            int y = stoneDTO.y();
            String color = stoneDTO.color();
            if(color.equals("BLACK")) {
                stones[x][y] = new Stone(x, y, StoneColor.BLACK);
            } else if(color.equals("WHITE")) {
                stones[x][y] = new Stone(x, y, StoneColor.WHITE);
            }
        }
    }

    public Stone[][] getGrid() {
        return stones;
    }

    public int getSize() {
        return size;
    }

    public Territory getTerritory() {
        return territory;
    }
}