package com.gogame.model;


public class Stone {
    private final Position position;
    private final StoneColor color;

    public Stone(Position position, StoneColor color) {
        this.position = position;
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public StoneColor getColor() {
        return color;
    }
}
