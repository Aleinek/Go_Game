package com.gogame.service;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.*;
import com.gogame.dto.response.BoardResponse;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for board-related operations.
 * <p>
 * Handles:
 * <ul>
 *   <li>Board state to DTO conversion</li>
 *   <li>Move validation (placeholder for future rules)</li>
 *   <li>Move application with capture detection</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Service
public class BoardService {
    
    /**
     * Converts board state to a response DTO.
     * 
     * @param gameId the game identifier
     * @param board the board to convert
     * @param moveNumber current move number
     * @param blackCaptured stones captured by black
     * @param whiteCaptured stones captured by white
     * @return BoardResponse with all stone positions and territory
     */
    public BoardResponse getBoardResponse(UUID gameId, Board board, int moveNumber, 
                                         int blackCaptured, int whiteCaptured) {
        List<BoardResponse.StoneInfo> stones = new ArrayList<>();
        Territory territory = board.getTerritory();
        
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                Position pos = new Position(x, y);
                Stone stone = board.getStoneAt(pos);
                if (stone != null && stone.getColor() != StoneColor.EMPTY) {
                    stones.add(new BoardResponse.StoneInfo(x, y, stone.getColor().toString()));
                }
            }
        }

        return new BoardResponse(gameId, board.getSize(), moveNumber, stones, blackCaptured, whiteCaptured,
                                 territory.getWhiteTerritory(), territory.getBlackTerritory(), territory.getNeutralTerritory());
    }
    
    /**
     * Validates a move before execution.
     * <p>
     * Placeholder for additional rule validation in future versions.
     * </p>
     * 
     * @param board the current board state
     * @param position the target position
     * @param color the stone color to place
     */
    public void validateMove(Board board, Position position, StoneColor color) {
        // PLACEHOLDER NA NASTEPNE LISTY
    }
    
    /**
     * Applies a move to the board and returns captured positions.
     * <p>
     * Captures are detected by comparing stone positions before and after
     * the move is placed.
     * </p>
     * 
     * @param board the board to modify
     * @param position where to place the stone
     * @param color the stone color
     * @param blackPlayer black player for capture counting
     * @param whitePlayer white player for capture counting
     * @return list of positions where stones were captured
     */
    public List<Position> applyMove(Board board, Position position, StoneColor color, 
                                    Player blackPlayer, Player whitePlayer) {
        Set<Position> stonesBefore = getAllStonePositions(board);
        
        board.placeStone(position, color);
        board.updateTerritory();

        Set<Position> stonesAfter = getAllStonePositions(board);
        
        Set<Position> capturedPositions = new HashSet<>(stonesBefore);
        capturedPositions.removeAll(stonesAfter);
        
        capturedPositions.remove(position);
        
        
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
    
    /**
     * Checks if a position on the board is empty.
     * 
     * @param board the board to check
     * @param position the position to check
     * @return true if the position has no stone
     */
    public boolean isEmpty(Board board, Position position) {
        return board.isEmpty(position);
    }
}
