package com.gogame.domain.model;

import java.util.HashSet;
import java.util.Set;

import com.gogame.domain.enums.StoneColor;


public class Chain {
    Set<Stone> stones;
    StoneColor color;
    int breaths;

    public Chain(Set<Stone> stones, StoneColor color) {
        this.stones = stones;
        this.color = color;
    }

    public Chain(Stone stone, StoneColor color) {
        this.stones = new HashSet<>();
        stones.add(stone);
        this.color = color;
    }

    public void addStone(Stone stone) {
        stones.add(stone);
    }

    public Set<Stone> getStones() {
        return stones;
    }

    public Chain merge(Chain other) {
        if (this.color != other.color) {
            throw new IllegalArgumentException("Cannot merge chains of different colors");
        }
        Set<Stone> mergedStones = new java.util.HashSet<>(this.stones);
        mergedStones.addAll(other.stones);
        return new Chain(mergedStones, this.color);
    }

}
