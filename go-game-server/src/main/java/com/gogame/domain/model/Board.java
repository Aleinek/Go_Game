package com.gogame.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.exception.InvalidMoveException.ErrorCode;

public class Board {
    private final int size;
    private final Stone[][] grid;
    private final Map<Position, Chain> chains;
    Player blackPlayer;
    Player whitePlayer;
    Territory territory;

    public Board(int size, Player blackPlayer, Player whitePlayer) {
        this.size = size;
        this.grid = new Stone[size][size];
        this.chains = new HashMap<>();
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.territory = new Territory(size);
    }

    public int getSize() {
        return size;
    }

    public Stone[][] getGrid() {
        return grid;
    }

    public Stone getStoneAt(Position position) {
        if (!position.isValid(size)) {
            throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS, position);
        }
        return grid[position.getX()][position.getY()];
    }

    public Chain getChainAt(Position position) {
        if (!position.isValid(size)) {
            throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS, position);
        }
        return chains.get(position);
    }

    public Chain getNewStoneChain(Stone stone) {
        Position position = stone.getPosition();
        StoneColor color = stone.getColor();
        Chain mergedChain = null;

        Set<Chain> chainSet = getAllyNeighbouringChainsSet(position, color);
        mergedChain = new Chain(stone, color);
        if(!chainSet.isEmpty())  {
            for (Chain chain : chainSet) {
                mergedChain = mergedChain.merge(chain);
            } 
        }

        return mergedChain;
    }

    public void placeStone(Position position, StoneColor color) {
        Stone stone = new Stone(position, color);
        Chain potentialChain = getNewStoneChain(stone); 

        if(!position.isValid(size)) 
            throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS);
        else if(!isEmpty(position)) 
            throw new InvalidMoveException(ErrorCode.POSITION_OCCUPIED);

        grid[position.getX()][position.getY()] = stone; // dodajemy kamienia na jego potencjalnie miejsce zeby algorytmy sprawdzajaca go widzialy
        
        int capturedChains = 0;
        Set<Chain> enemyChains = getEnemyNeighbouringChainsSet(position, color); 
        for(Chain enemyChain : enemyChains) {
            if(isCaptured(enemyChain)) {
                capturedChains++;
                removeChain(enemyChain, color);
            }
        }

        if(moveIsSuicidal(potentialChain) && capturedChains <= 0) {
            grid[position.getX()][position.getY()] = null;
            throw new InvalidMoveException(ErrorCode.SUICIDE_MOVE);
        } 
        
        for (Stone s : potentialChain.getStones()) {
                chains.put(s.getPosition(), potentialChain);
        }                  

        
    }

    public List<Position> getEmptyNeighboursPositions(Position position) {
        List<Position> positions = new ArrayList<>();
        for (Position neighbour : position.getNeighbors()) {
            if(neighbour.isValid(size)) {
                if(isEmpty(neighbour))
                    positions.add(neighbour);
            }
        }
        return positions;
    }

    public int getBreaths(Chain chain) {
        Set<Position> breaths = new HashSet<>();
        for (Stone stone : chain.getStones()) {
            Position position = stone.getPosition();
            breaths.addAll(getEmptyNeighboursPositions(position));
        }
        return breaths.size();
    }

    // te logike bedzie trzeba zrobic na chainach nie na kamieniach
    public boolean moveIsSuicidal(Chain chain) {
        if(getBreaths(chain) > 0) return false;
        else return true;
        // teraz musimy sie dowiedziec czy nasz ruch robi jakis capture ale moze najpierw stestuje gierke
        
    }

    public boolean positionContainChain(Position pos) {
        return getChainAt(pos) != null;
    }

    public Set<Chain> getAllyNeighbouringChainsSet(Position pos, StoneColor color) {
        Set<Chain> allies = new HashSet<>();
        for(Position neighbour : pos.getNeighbors()) {
            if(neighbour.isValid(size)) { // pozycja nie jest poza plansza
                if(positionContainChain(neighbour))
                    if(getStoneAt(neighbour).getColor() == color)
                        allies.add(getChainAt(neighbour));
            } 
        }
        return allies;
    }

    public Set<Chain> getEnemyNeighbouringChainsSet(Position pos, StoneColor color) {
        Set<Chain> enemies = new HashSet<>();
        for(Position neighbour : pos.getNeighbors()) {
            if(neighbour.isValid(size)) { // pozycja nie jest poza plansza
                if(positionContainChain(neighbour))
                    if(getStoneAt(neighbour).getColor() != color)
                        enemies.add(getChainAt(neighbour));
            } 
        }
        return enemies;
    }

    public boolean isCaptured(Chain chain) {
        return (getBreaths(chain) <= 0);
    }

    public void removeChain(Chain chain, StoneColor enemyColor) {
        Set<Stone> capturedStones = chain.getStones();
        int capturedStonesCount = capturedStones.size();
        Player enemyPlayer = (enemyColor == StoneColor.BLACK ? blackPlayer : whitePlayer);
        enemyPlayer.addCaptured(capturedStonesCount);
        for(Stone capturedStone : capturedStones) {
            Position capturedStonePos = capturedStone.getPosition();
            chains.remove(capturedStonePos);
            grid[capturedStonePos.getX()][capturedStonePos.getY()] = null;
        }
    }

    public boolean isEmpty(Position position) {
        return getStoneAt(position) == null;
    }

    public void updateTerritory() {
        territory.setBlackTerritory(0);
        territory.setWhiteTerritory(0);
        territory.setNeutralTerritory(0);

        boolean[][] visited = new boolean[size][size];

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                Position currentPos = new Position(x, y);
                
                // jesli pole jest puste i jeszcze nie było sprawdzone w tej turze
                if (isEmpty(currentPos) && !visited[x][y]) {
                    analyzeTerritoryRegion(currentPos, visited);
                }
            }
        }
    }

    /**
     * Metoda pomocnicza wykonująca algorytm Flood Fill (BFS) dla spójnego obszaru pustych pól.
     */
    private void analyzeTerritoryRegion(Position startNode, boolean[][] visited) {
        Queue<Position> queue = new LinkedList<>();
        queue.add(startNode);
        visited[startNode.getX()][startNode.getY()] = true;

        int emptyPointsCount = 0;
        boolean touchesBlack = false;
        boolean touchesWhite = false;

        while (!queue.isEmpty()) {
            Position current = queue.poll();
            emptyPointsCount++;

            // Sprawdzamy sąsiadów
            for (Position neighbor : current.getNeighbors()) {
                if (!neighbor.isValid(size)) {
                    continue; // Pomijamy sąsiadów poza planszą
                }

                if (isEmpty(neighbor)) {
                    if (!visited[neighbor.getX()][neighbor.getY()]) {
                        visited[neighbor.getX()][neighbor.getY()] = true;
                        queue.add(neighbor);
                    }
                } else {
                    Stone stone = getStoneAt(neighbor);
                    if (stone.getColor() == StoneColor.BLACK) {
                        touchesBlack = true;
                    } else if (stone.getColor() == StoneColor.WHITE) {
                        touchesWhite = true;
                    }
                }
            }
        }

        // decyzja, do kogo należy terytorium
        if (touchesBlack && !touchesWhite) {
            int currentTotal = territory.getBlackTerritory();
            territory.setBlackTerritory(currentTotal + emptyPointsCount);
        } else if (!touchesBlack && touchesWhite) {
            int currentTotal = territory.getWhiteTerritory();
            territory.setWhiteTerritory(currentTotal + emptyPointsCount);
        } else {
            int currentTotal = territory.getNeutralTerritory();
            territory.setNeutralTerritory(currentTotal + emptyPointsCount);
        }
    }

    public Territory getTerritory() {
        return territory;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

}
