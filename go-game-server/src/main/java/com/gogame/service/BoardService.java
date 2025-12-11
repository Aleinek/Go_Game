package com.gogame.service;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.*;
import com.gogame.dto.response.BoardResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BoardService {
    
    public BoardResponse getBoardResponse(UUID gameId, Board board, int moveNumber, 
                                         int blackCaptured, int whiteCaptured) {
        List<BoardResponse.StoneInfo> stones = new ArrayList<>();
        
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                Position pos = new Position(x, y);
                Stone stone = board.getStoneAt(pos);
                if (stone != null && stone.getColor() != StoneColor.EMPTY) {
                    stones.add(new BoardResponse.StoneInfo(x, y, stone.getColor().toString()));
                }
            }
        }
        
        return new BoardResponse(gameId, board.getSize(), moveNumber, stones, blackCaptured, whiteCaptured);
    }
    
    public void validateMove(Board board, Position position, StoneColor color) {
        // PLACEHOLDER NA NASTEPNE LISTY
    }
    
    public List<Position> applyMove(Board board, Position position, StoneColor color, 
                                    Player blackPlayer, Player whitePlayer) {
        Set<Position> stonesBefore = getAllStonePositions(board);
        
        board.placeStone(position, color);
        
        Set<Position> stonesAfter = getAllStonePositions(board);
        
        Set<Position> capturedPositions = new HashSet<>(stonesBefore);
        capturedPositions.removeAll(stonesAfter);
        
        capturedPositions.remove(position);
        
        int capturedCount = capturedPositions.size();
        if (capturedCount > 0) {
            if (color == StoneColor.BLACK) {
                blackPlayer.addCaptured(capturedCount);
            } else {
                whitePlayer.addCaptured(capturedCount);
            }
        }
        
        return new ArrayList<>(capturedPositions);
    }
    
    private Set<Position> getAllStonePositions(Board board) {
        Set<Position> positions = new HashSet<>();
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                Position pos = new Position(x, y);
                Stone stone = board.getStoneAt(pos);
                if (stone != null && stone.getColor() != StoneColor.EMPTY) {
                    positions.add(pos);
                }
            }
        }
        return positions;
    }
    
    public boolean isEmpty(Board board, Position position) {
        return board.isEmpty(position);
    }
}
