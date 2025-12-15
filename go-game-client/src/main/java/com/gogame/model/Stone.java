package com.gogame.model;


public class Stone {
    private final int x;
    private final int y;
    private final StoneColor color;

    public Stone(int x, int y, StoneColor color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public StoneColor getColor() {
        return color;
    }
}
