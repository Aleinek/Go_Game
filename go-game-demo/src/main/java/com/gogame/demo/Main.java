package com.gogame.demo;

import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int boardSize = 19;
        int maxIdx = boardSize - 1;
        // zainicjowac jakiegos boarda i napisac funkcje board printer
        Board board = new Board(19);
        // polozmy sobie teraz losowo po 50 kamieni
        Random random = new Random();
        // biale
        for(int i = 0; i < 50; i++) {
            int x = random.nextInt(boardSize);
            int y = random.nextInt(boardSize);
            board.placeStone(new Position(x, y), StoneColor.WHITE);
        }
        // czarne
        for(int i = 0; i < 50; i++) {
            int x = random.nextInt(boardSize);
            int y = random.nextInt(boardSize);
            board.placeStone(new Position(x, y), StoneColor.BLACK);
        }

        BoardPrinter.printBoard(board);
    }
}