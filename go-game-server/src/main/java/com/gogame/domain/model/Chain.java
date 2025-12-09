package com.gogame.domain.model;

import java.util.Set;
import com.gogame.domain.enums.StoneColor;


public class Chain {
    Set<Stone> stones;
    StoneColor color;

    public Chain(Set<Stone> stones, StoneColor color) {
        this.stones = stones;
        this.color = color;
    }

    public void addStone(Stone stone) {
        stones.add(stone);
    }

    public Set<Stone> getStones() {
        return stones;
    }

    // oddechy
    public Set<Position> getLiberties(Board board) {
        Set<Position> liberties = new java.util.HashSet<>();
        for (Stone stone : stones) {
            for (Position neighbor : stone.getPosition().getNeighbors()) {
                // TODO[] lepiej chyba jakby getNeighbours od razu zwracalo tylko valid kamienie
                if (neighbor.isValid(board.getSize()) && board.getStoneAt(neighbor) == null) {
                    liberties.add(neighbor);
                }
            }
        }
        return liberties;
    }

    public Chain merge(Chain other) {
        if (this.color != other.color) {
            throw new IllegalArgumentException("Cannot merge chains of different colors");
        }
        Set<Stone> mergedStones = new java.util.HashSet<>(this.stones);
        mergedStones.addAll(other.stones);
        return new Chain(mergedStones, this.color);
    }

    public boolean isCaptured(Board board) {
        return getLiberties(board).isEmpty();
    }
}
