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
        if(chainSet.isEmpty())  
            mergedChain = new Chain(stone, color);
        else {
            // Najpierw połóż kamień jako łańcuch tymczasowy
            mergedChain = new Chain(stone, color);

            for (Chain chain : chainSet) {
                mergedChain = mergedChain.merge(chain);
            } 
        }

        return mergedChain;
    }

    public void placeStone(Position position, StoneColor color) {
        Stone stone = new Stone(position, color);
        Chain potentialChain = getNewStoneChain(stone);

        if(!position.isValid(size)) {
            throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS);
        } else if(!isEmpty(position)) {
            throw new InvalidMoveException(ErrorCode.POSITION_OCCUPIED);
        } else if(moveIsSuicidal(potentialChain)) {
            throw new InvalidMoveException(ErrorCode.SUICIDE_MOVE);
        } 
        
        for (Stone s : potentialChain.getStones()) {
                chains.put(s.getPosition(), potentialChain);
        }      
            
        grid[position.getX()][position.getY()] = stone;
    }

    public void updateBoardAfterMove() {

    }

    public int getLiberties(Chain chain) {
        return 0;
    }

    // te logike bedzie trzeba zrobic na chainach nie na kamieniach
    public boolean moveIsSuicidal(Chain chain) {
        for(Position neighbour : position.getNeighbors()) {
            if(neighbour.isValid(size)) {
                Stone stone = getStoneAt(neighbour);
                if(stone == null)
                    return false;
                else if(stone.getColor() == color)
                    return false;      
            }
        }
        return true;
    }

    public boolean positionContainChain(Position pos) {
        return getChainAt(pos) != null;
    }

    public Set<Chain> getAllyNeighbouringChainsList(Position pos, StoneColor color) {
        Set<Chain> allies = new HashSet<>();
        for(Position neighbour : pos.getNeighbors()) {
            if(neighbour.isValid(size)) { // pozycja nie jest poza plansza
                if(positionContainChain(neighbour) && getStoneAt(neighbour).getColor() == color)
                    allies.add(getChainAt(neighbour));
            } 
        }
        return allies;
    }


    public boolean isEmpty(Position position) {
        return getStoneAt(position) == null;
    }

}
