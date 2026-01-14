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
 * Implements Japanese rules: Territory + Prisoners + Dead stones - Komi
 */
@Service
public class NegotiationService {

    // Validation thresholds for dead stone marking
    private static final int MAX_LIBERTIES_FOR_DEAD = 3;

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
     * A chain is considered likely dead if:
     * - It has 2 or fewer liberties AND
     * - It is surrounded primarily by opponent stones
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
     * Blocks clearly illogical markings.
     * 
     * @param board the current board
     * @param chainInfo the chain to validate
     * @param proposedStatus the proposed new status
     * @throws InvalidNegotiationException if the marking is clearly invalid
     */
    public void validateDeadMarking(Board board, ChainInfo chainInfo, DeadStoneStatus proposedStatus) {
        if (proposedStatus != DeadStoneStatus.DEAD) {
            // Marking as ALIVE is always allowed
            return;
        }

        int liberties = chainInfo.getLiberties();
        
        // Rule 1: Cannot mark chains with many liberties as dead
        if (liberties > MAX_LIBERTIES_FOR_DEAD) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.CHAIN_HAS_TOO_MANY_LIBERTIES,
                chainInfo.getChainId()
            );
        }

        // Rule 2: Check if chain is actually surrounded by opponent
        StoneColor chainColor = chainInfo.getColor();
        boolean hasSurroundingEnemy = false;
        boolean hasSurroundingAlly = false;
        
        // Find the actual chain on the board
        for (Position pos : chainInfo.getPositions()) {
            Chain actualChain = board.getChainAt(pos);
            if (actualChain != null) {
                Set<Chain> enemies = board.getEnemyNeighbouringChainsSet(pos, chainColor);
                Set<Chain> allies = board.getAllyNeighbouringChainsSet(pos, chainColor);
                
                if (!enemies.isEmpty()) {
                    hasSurroundingEnemy = true;
                }
                if (!allies.isEmpty()) {
                    hasSurroundingAlly = true;
                }
            }
        }
        
        // If chain has no surrounding enemy stones, it cannot be considered dead
        if (!hasSurroundingEnemy && liberties > 0) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.CHAIN_NOT_SURROUNDED,
                chainInfo.getChainId()
            );
        }
        
        // If chain is surrounded only by allies and has liberties, likely not dead
        if (hasSurroundingAlly && !hasSurroundingEnemy && liberties > 0) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.CHAIN_IS_OWN_COLOR,
                chainInfo.getChainId()
            );
        }
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
     * BFS flood-fill to analyze a territory region.
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
