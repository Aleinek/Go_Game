package com.gogame.demo;

import java.util.Random;
import java.util.Scanner;

import com.gogame.demo.InvalidMoveException.ErrorCode;

public class Main {
    public static void main(String[] args) {
        int boardSize = 9;
        int maxIdx = boardSize - 1;
        
        Random random = new Random();
        Player blackPlayer = new Player(null, "czarny", StoneColor.BLACK);
        Player whitePlayer = new Player(null, "bialy", StoneColor.WHITE);
        Board board = new Board(boardSize, whitePlayer, blackPlayer);
        Game game = new Game(blackPlayer, whitePlayer, boardSize, board);

        BoardPrinter.printBoard(board);
        
        // void getPlayerToMove()
        while(!game.isGameOver()) {
            while(true) {
                try {
                    System.out.println("Ruch gracza " + (game.getCurrentTurn() == StoneColor.BLACK ? "czarnego" : "bialego"));
                    int x = CLIController.getX(boardSize);
                    int y = CLIController.getY(boardSize);
                    game.makeMove(new Position(x - 1, y - 1));
                    break;
                } catch (Exception e) {
                    System.out.println("Podany ruch jest nielegalny, sprobuj ponownie!");
                }
            }
            board.updateTerritory();
            BoardPrinter.printBoard(board);                    
            }
    }
    
    public static void printGameStatus(Board board) {
        BoardPrinter.printBoard(board);

    }
}