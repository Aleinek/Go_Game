package com.gogame.bot;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.Board;
import com.gogame.domain.model.Chain;
import com.gogame.domain.model.Position;
import com.gogame.domain.model.Stone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Strategy class for bot move calculation in Go game.
 * <p>
 * Implements a simple heuristic-based strategy that prioritizes:
 * <ol>
 *   <li>Defensive moves - saving own chains with 1 liberty</li>
 *   <li>Capture moves - capturing opponent chains with 1 liberty</li>
 *   <li>Development moves - expanding territory and building chains</li>
 * </ol>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class BotStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(BotStrategy.class);
    
    /**
     * Finds the best move for the bot based on heuristics.
     * 
     * @param board the current board state
     * @param botColor the color of the bot's stones
     * @param moveCount current number of moves in the game
     * @return the best position to play, or null if bot should pass
     */
    public Position findBestMove(Board board, StoneColor botColor, int moveCount) {
        int size = board.getSize();
        
        // Priority 1: Defend own chains with only 1 liberty (atari)
        Position defensiveMove = findDefensiveMove(board, botColor);
        if (defensiveMove != null) {
            log.debug("Bot choosing defensive move at ({}, {})", defensiveMove.getX(), defensiveMove.getY());
            return defensiveMove;
        }
        
        // Priority 2: Capture opponent chains with only 1 liberty
        Position captureMove = findCaptureMove(board, botColor);
        if (captureMove != null) {
            log.debug("Bot choosing capture move at ({}, {})", captureMove.getX(), captureMove.getY());
            return captureMove;
        }
        
        // Priority 3: Development move - extend chains or claim territory
        Position developMove = findDevelopmentMove(board, botColor, moveCount);
        if (developMove != null) {
            log.debug("Bot choosing development move at ({}, {})", developMove.getX(), developMove.getY());
            return developMove;
        }
        
        // No good moves found - should pass
        log.debug("Bot found no good moves, will pass");
        return null;
    }
    
    /**
     * Finds a move to save an own chain that is in atari (1 liberty).
     */
    private Position findDefensiveMove(Board board, StoneColor botColor) {
        Set<Chain> visitedChains = new HashSet<>();
        int size = board.getSize();
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Position pos = new Position(x, y);
                Stone stone = board.getStoneAt(pos);
                
                if (stone != null && stone.getColor() == botColor) {
                    Chain chain = board.getChainAt(pos);
                    if (chain != null && !visitedChains.contains(chain)) {
                        visitedChains.add(chain);
                        
                        int liberties = board.getBreaths(chain);
                        if (liberties == 1) {
                            // Chain is in atari! Find a move to add liberty
                            Position escapeMove = findEscapeMove(board, chain, botColor);
                            if (escapeMove != null) {
                                return escapeMove;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Finds a move to escape from atari by adding liberties.
     */
    private Position findEscapeMove(Board board, Chain chain, StoneColor color) {
        int size = board.getSize();
        
        // Find the single liberty of the chain
        for (Stone stone : chain.getStones()) {
            for (Position neighbor : stone.getPosition().getNeighbors()) {
                if (neighbor.isValid(size) && board.isEmpty(neighbor)) {
                    // Check if this move is legal (not suicide after placing)
                    if (isLegalMove(board, neighbor, color)) {
                        return neighbor;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Finds a move to capture an opponent chain that is in atari.
     */
    private Position findCaptureMove(Board board, StoneColor botColor) {
        StoneColor opponentColor = (botColor == StoneColor.BLACK) ? StoneColor.WHITE : StoneColor.BLACK;
        Set<Chain> visitedChains = new HashSet<>();
        int size = board.getSize();
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Position pos = new Position(x, y);
                Stone stone = board.getStoneAt(pos);
                
                if (stone != null && stone.getColor() == opponentColor) {
                    Chain chain = board.getChainAt(pos);
                    if (chain != null && !visitedChains.contains(chain)) {
                        visitedChains.add(chain);
                        
                        int liberties = board.getBreaths(chain);
                        if (liberties == 1) {
                            // Opponent chain is in atari! Find the capturing move
                            Position capturePos = findLastLiberty(board, chain);
                            if (capturePos != null && isLegalMove(board, capturePos, botColor)) {
                                return capturePos;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Finds the last liberty of a chain (for capturing).
     */
    private Position findLastLiberty(Board board, Chain chain) {
        int size = board.getSize();
        
        for (Stone stone : chain.getStones()) {
            for (Position neighbor : stone.getPosition().getNeighbors()) {
                if (neighbor.isValid(size) && board.isEmpty(neighbor)) {
                    return neighbor;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds a development move to expand or strengthen position.
     */
    private Position findDevelopmentMove(Board board, StoneColor botColor, int moveCount) {
        int size = board.getSize();
        List<ScoredPosition> candidates = new ArrayList<>();
        
        // Evaluate all empty positions
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Position pos = new Position(x, y);
                if (board.isEmpty(pos) && isLegalMove(board, pos, botColor)) {
                    int score = evaluatePosition(board, pos, botColor, moveCount);
                    if (score > 0) {
                        candidates.add(new ScoredPosition(pos, score));
                    }
                }
            }
        }
        
        if (candidates.isEmpty()) {
            return null;
        }
        
        // Sort by score descending and pick the best
        candidates.sort((a, b) -> Integer.compare(b.score, a.score));
        
        // Add small randomness among top moves (top 3)
        int maxIndex = Math.min(3, candidates.size());
        int chosenIndex = new Random().nextInt(maxIndex);
        
        return candidates.get(chosenIndex).position;
    }
    
    /**
     * Evaluates the strategic value of a position.
     */
    private int evaluatePosition(Board board, Position pos, StoneColor botColor, int moveCount) {
        int score = 10; // Base score
        int size = board.getSize();
        StoneColor opponentColor = (botColor == StoneColor.BLACK) ? StoneColor.WHITE : StoneColor.BLACK;
        
        int x = pos.getX();
        int y = pos.getY();
        
        // Opening phase: prefer points away from edges
        boolean isOpening = moveCount < (size * 2);
        
        // Star points and center are good in opening
        if (isOpening) {
            // Penalize first and second line in opening
            if (isOnEdge(x, y, size, 1)) {
                score -= 15;
            } else if (isOnEdge(x, y, size, 2)) {
                score -= 5;
            }
            
            // Bonus for 3rd and 4th line
            if (isOnLine(x, y, size, 3) || isOnLine(x, y, size, 4)) {
                score += 5;
            }
            
            // Star points bonus
            if (isStarPoint(x, y, size)) {
                score += 10;
            }
        }
        
        // Count friendly and enemy neighbors
        int friendlyNeighbors = 0;
        int enemyNeighbors = 0;
        int emptyNeighbors = 0;
        
        for (Position neighbor : pos.getNeighbors()) {
            if (!neighbor.isValid(size)) {
                continue;
            }
            
            if (board.isEmpty(neighbor)) {
                emptyNeighbors++;
            } else {
                Stone stone = board.getStoneAt(neighbor);
                if (stone.getColor() == botColor) {
                    friendlyNeighbors++;
                } else if (stone.getColor() == opponentColor) {
                    enemyNeighbors++;
                }
            }
        }
        
        // Bonus for extending from own stones
        score += friendlyNeighbors * 3;
        
        // Small bonus for contact with enemy (fighting)
        score += enemyNeighbors * 2;
        
        // Prefer positions with more liberties
        score += emptyNeighbors * 2;
        
        // Avoid playing where we would have very few liberties
        if (emptyNeighbors == 0 && friendlyNeighbors == 0) {
            score -= 20;
        }
        
        // Bonus for extending chains that are under pressure
        if (friendlyNeighbors > 0) {
            for (Position neighbor : pos.getNeighbors()) {
                if (neighbor.isValid(size)) {
                    Stone stone = board.getStoneAt(neighbor);
                    if (stone != null && stone.getColor() == botColor) {
                        Chain chain = board.getChainAt(neighbor);
                        if (chain != null) {
                            int liberties = board.getBreaths(chain);
                            if (liberties <= 2) {
                                score += 5; // Help chains with few liberties
                            }
                        }
                    }
                }
            }
        }
        
        return score;
    }
    
    /**
     * Checks if a position is on a specific line from the edge.
     */
    private boolean isOnEdge(int x, int y, int size, int line) {
        return x == line - 1 || x == size - line || y == line - 1 || y == size - line;
    }
    
    private boolean isOnLine(int x, int y, int size, int line) {
        return x == line - 1 || x == size - line || y == line - 1 || y == size - line;
    }
    
    /**
     * Checks if position is a star point (hoshi).
     */
    private boolean isStarPoint(int x, int y, int size) {
        if (size == 9) {
            // 9x9: center and corners at (2,2), (2,6), (4,4), (6,2), (6,6)
            return (x == 2 && y == 2) || (x == 2 && y == 6) ||
                   (x == 4 && y == 4) ||
                   (x == 6 && y == 2) || (x == 6 && y == 6);
        } else if (size == 13) {
            // 13x13: star points at (3,3), (3,9), (6,6), (9,3), (9,9)
            return (x == 3 && y == 3) || (x == 3 && y == 9) ||
                   (x == 6 && y == 6) ||
                   (x == 9 && y == 3) || (x == 9 && y == 9);
        } else if (size == 19) {
            // 19x19: star points
            int[] starLines = {3, 9, 15};
            for (int sx : starLines) {
                for (int sy : starLines) {
                    if (x == sx && y == sy) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Checks if a move is legal (not suicide and not ko).
     */
    private boolean isLegalMove(Board board, Position pos, StoneColor color) {
        // Check basic requirements
        if (!pos.isValid(board.getSize()) || !board.isEmpty(pos)) {
            return false;
        }
        
        // Check ko rule
        if (pos.equals(board.getKoPosition())) {
            return false;
        }
        
        // Check for suicide (simplified - assumes capturing is checked)
        int emptyNeighbors = 0;
        int friendlyNeighborsWithMultipleLiberties = 0;
        boolean willCapture = false;
        
        StoneColor opponentColor = (color == StoneColor.BLACK) ? StoneColor.WHITE : StoneColor.BLACK;
        
        for (Position neighbor : pos.getNeighbors()) {
            if (!neighbor.isValid(board.getSize())) {
                continue;
            }
            
            if (board.isEmpty(neighbor)) {
                emptyNeighbors++;
            } else {
                Stone stone = board.getStoneAt(neighbor);
                Chain chain = board.getChainAt(neighbor);
                
                if (chain != null) {
                    if (stone.getColor() == color) {
                        // Friendly chain - check if it has more than 1 liberty
                        if (board.getBreaths(chain) > 1) {
                            friendlyNeighborsWithMultipleLiberties++;
                        }
                    } else {
                        // Enemy chain - check if we would capture it
                        if (board.getBreaths(chain) == 1) {
                            willCapture = true;
                        }
                    }
                }
            }
        }
        
        // Move is legal if:
        // - Has at least one empty neighbor (liberty after placement), or
        // - Connects to a friendly chain with multiple liberties, or
        // - Captures an enemy chain
        return emptyNeighbors > 0 || friendlyNeighborsWithMultipleLiberties > 0 || willCapture;
    }
    
    /**
     * Helper class for scored positions.
     */
    private static class ScoredPosition {
        final Position position;
        final int score;
        
        ScoredPosition(Position position, int score) {
            this.position = position;
            this.score = score;
        }
    }
}
