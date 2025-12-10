package com.gogame.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gogame.demo.InvalidMoveException.ErrorCode;

public class Board {
    private final int size;
    private final Stone[][] grid;
    private final Map<Position, Chain> chains;

    public Board(int size) {
        this.size = size;
        this.grid = new Stone[size][size];
        this.chains = new HashMap<>();
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

        Set<Chain> chainSet = getAllyNeighbouringChainsList(position, color);
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

        grid[position.getX()][position.getY()] = stone;
        if(moveIsSuicidal(potentialChain)) {
            grid[position.getX()][position.getY()] = null;
            throw new InvalidMoveException(ErrorCode.SUICIDE_MOVE);
        } 
        
        for (Stone s : potentialChain.getStones()) {
                chains.put(s.getPosition(), potentialChain);
        }                  
    }

    public void updateBoardAfterMove() {

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

    public Set<Chain> getAllyNeighbouringChainsList(Position pos, StoneColor color) {
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


    public boolean isEmpty(Position position) {
        return getStoneAt(position) == null;
    }

}
