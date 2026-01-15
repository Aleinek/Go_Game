package com.gogame.service;

import com.gogame.domain.enums.DeadStoneStatus;
import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidNegotiationException;
import com.gogame.domain.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service handling the scoring negotiation phase of a Go game.
 * <p>
 * Implements Japanese rules scoring: Territory + Prisoners + Dead stones - Komi
 * </p>
 * <p>
 * The negotiation flow:
 * <ol>
 *   <li>Game enters NEGOTIATING state after both players pass</li>
 *   <li>Service analyzes board and suggests dead chains using heuristics</li>
 *   <li>Players can mark chains as DEAD or ALIVE</li>
 *   <li>Both players must accept the marking before final scoring</li>
 *   <li>Final score is calculated and winner determined</li>
 * </ol>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Service
public class NegotiationService {

    /**
     * Analyzes the board and suggests chains that are likely dead.
     * Uses heuristics: chains with few liberties surrounded by opponent.
     * 
     * @param board the current board state
     * @return list of ChainInfo objects with suggested dead chains marked
     */
    public List<ChainInfo> analyzeAndSuggestDeadChains(Board board) {
        List<ChainInfo> chainInfoList = new ArrayList<>();
        Set<Chain> processedChains = new HashSet<>();
        int chainIdCounter = 1;

        // Iterate through all positions and collect unique chains
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                Position pos = new Position(x, y);
                Chain chain = board.getChainAt(pos);
                
                if (chain != null && !processedChains.contains(chain)) {
                    processedChains.add(chain);
                    
                    int liberties = board.getBreaths(chain);
                    ChainInfo chainInfo = new ChainInfo(chainIdCounter++, chain, liberties);
                    
                    // Apply heuristics to suggest dead chains
                    if (isLikelyDead(board, chain, liberties)) {
                        chainInfo.setStatus(DeadStoneStatus.DEAD);
                    }
                    
                    chainInfoList.add(chainInfo);
                }
            }
        }
        
        return chainInfoList;
    }

    /**
     * Heuristic to determine if a chain is likely dead.
     * <p>
     * A chain is considered likely dead if:
     * <ul>
     *   <li>It has 2 or fewer liberties AND</li>
     *   <li>It is surrounded primarily by opponent stones</li>
     * </ul>
     * </p>
     * <p>
     * This is just a suggestion - players can override during negotiation.
     * </p>
     * 
     * @param board the current board state
     * @param chain the chain to evaluate
     * @param liberties the chain's liberty count
     * @return true if the chain appears to be dead
     */
    private boolean isLikelyDead(Board board, Chain chain, int liberties) {
        if (liberties > 2) {
            return false;
        }

        StoneColor chainColor = chain.getColor();
        int enemyNeighborCount = 0;
        int allyNeighborCount = 0;
        
        // Count neighboring chains by color
        Set<Chain> neighborChains = new HashSet<>();
        for (Stone stone : chain.getStones()) {
            Position pos = stone.getPosition();
            neighborChains.addAll(board.getEnemyNeighbouringChainsSet(pos, chainColor));
            neighborChains.addAll(board.getAllyNeighbouringChainsSet(pos, chainColor));
        }
        
        for (Stone stone : chain.getStones()) {
            Position pos = stone.getPosition();
            Set<Chain> enemies = board.getEnemyNeighbouringChainsSet(pos, chainColor);
            Set<Chain> allies = board.getAllyNeighbouringChainsSet(pos, chainColor);
            enemyNeighborCount += enemies.size();
            allyNeighborCount += allies.size();
        }
        
        // Chain is likely dead if surrounded more by enemies than allies
        // and has few liberties
        return enemyNeighborCount > allyNeighborCount && liberties <= 2;
    }

    /**
     * Validates whether marking a chain as dead is logical.
     * All markings are now allowed - players have full control during negotiation.
     * 
     * @param board the current board
     * @param chainInfo the chain to validate
     * @param proposedStatus the proposed new status
     * @throws InvalidNegotiationException if the marking is clearly invalid
     */
    public void validateDeadMarking(Board board, ChainInfo chainInfo, DeadStoneStatus proposedStatus) {
        // All markings are allowed - no validation restrictions
        // Players can freely mark any chain as dead or alive during negotiation
    }

    /**
     * Calculates territory considering dead stones as removed.
     * Dame (neutral) points are not counted.
     * 
     * @param board the current board
     * @param negotiationState the current negotiation state with dead markings
     * @return updated Territory object
     */
    public Territory calculateTerritoryWithDeadStones(Board board, NegotiationState negotiationState) {
        // Create a virtual board state with dead stones removed
        Set<Position> deadPositions = new HashSet<>();
        for (ChainInfo chainInfo : negotiationState.getAllChainInfos().values()) {
            if (chainInfo.getStatus() == DeadStoneStatus.DEAD) {
                deadPositions.addAll(chainInfo.getPositions());
            }
        }
        
        // Recalculate territory treating dead stone positions as empty
        return calculateTerritoryExcludingPositions(board, deadPositions);
    }

    /**
     * Calculates territory treating specified positions as empty.
     * <p>
     * Used to simulate what the board would look like with dead stones removed.
     * Uses flood-fill algorithm to identify territory regions.
     * </p>
     * 
     * @param board the current board state
     * @param excludedPositions positions to treat as empty (dead stones)
     * @return Territory object with calculated territory counts
     */
    private Territory calculateTerritoryExcludingPositions(Board board, Set<Position> excludedPositions) {
        int size = board.getSize();
        Territory territory = new Territory(size);
        boolean[][] visited = new boolean[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Position pos = new Position(x, y);
                boolean isEmpty = board.isEmpty(pos) || excludedPositions.contains(pos);
                
                if (isEmpty && !visited[x][y]) {
                    analyzeTerritoryRegion(board, pos, visited, territory, excludedPositions);
                }
            }
        }
        
        return territory;
    }

    /**
     * BFS flood-fill to analyze a single territory region.
     * <p>
     * Starts from an empty position and expands to all connected empty positions.
     * Determines ownership based on which colors border the region:
     * <ul>
     *   <li>Only BLACK borders → BLACK territory</li>
     *   <li>Only WHITE borders → WHITE territory</li>
     *   <li>Both colors border → Dame (neutral)</li>
     *   <li>No borders → Dame (isolated region)</li>
     * </ul>
     * </p>
     * 
     * @param board the game board
     * @param startNode starting position for flood-fill
     * @param visited tracking array for visited positions
     * @param territory Territory object to update with results
     * @param excludedPositions positions treated as empty (dead stones)
     */
    private void analyzeTerritoryRegion(Board board, Position startNode, boolean[][] visited, 
                                        Territory territory, Set<Position> excludedPositions) {
        int size = board.getSize();
        Queue<Position> queue = new LinkedList<>();
        queue.add(startNode);
        visited[startNode.getX()][startNode.getY()] = true;

        int emptyPointsCount = 0;
        boolean touchesBlack = false;
        boolean touchesWhite = false;

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            emptyPointsCount++;

            for (Position neighbor : current.getNeighbors()) {
                if (!neighbor.isValid(size)) {
                    continue;
                }

                boolean neighborIsEmpty = board.isEmpty(neighbor) || excludedPositions.contains(neighbor);
                
                if (neighborIsEmpty) {
                    if (!visited[neighbor.getX()][neighbor.getY()]) {
                        visited[neighbor.getX()][neighbor.getY()] = true;
                        queue.add(neighbor);
                    }
                } else {
                    Stone stone = board.getStoneAt(neighbor);
                    if (stone != null) {
                        if (stone.getColor() == StoneColor.BLACK) {
                            touchesBlack = true;
                        } else if (stone.getColor() == StoneColor.WHITE) {
                            touchesWhite = true;
                        }
                    }
                }
            }
        }

        // Assign territory - dame (touching both or neither) is neutral
        if (touchesBlack && !touchesWhite) {
            territory.setBlackTerritory(territory.getBlackTerritory() + emptyPointsCount);
        } else if (!touchesBlack && touchesWhite) {
            territory.setWhiteTerritory(territory.getWhiteTerritory() + emptyPointsCount);
        } else {
            // Dame - points touching both colors or isolated empty regions
            territory.setNeutralTerritory(territory.getNeutralTerritory() + emptyPointsCount);
        }
    }

    /**
     * Calculates the final score using Japanese rules.
     * Score = Territory + Prisoners (captured during game) + Dead opponent stones - Komi
     * 
     * @param game the game to score
     * @return ScoreResult with complete breakdown
     */
    public ScoreResult calculateFinalScore(Game game) {
        if (game.status != GameStatus.NEGOTIATING || game.negotiationState == null) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.GAME_NOT_IN_NEGOTIATION
            );
        }

        NegotiationState state = game.negotiationState;
        
        // Calculate territory with dead stones considered as removed
        Territory territory = calculateTerritoryWithDeadStones(game.board, state);
        
        // Count dead stones for each color
        int blackDeadStones = state.countDeadStones(StoneColor.BLACK);
        int whiteDeadStones = state.countDeadStones(StoneColor.WHITE);
        
        // Get prisoners captured during the game
        int blackPrisoners = game.blackPlayer.getCapturedStones();
        int whitePrisoners = game.whitePlayer.getCapturedStones();
        
        return new ScoreResult(
            territory.getBlackTerritory(),
            territory.getWhiteTerritory(),
            blackPrisoners,
            whitePrisoners,
            blackDeadStones,
            whiteDeadStones,
            game.komi
        );
    }
}
