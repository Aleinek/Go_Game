package com.gogame;

import java.util.Scanner;

import com.gogame.model.InvalidMoveException;
import com.gogame.model.InvalidMoveException.ErrorCode;
import com.gogame.model.MoveType;
import com.gogame.model.Position;


public class CLIController {

    private static final Scanner scanner = new Scanner(System.in);

    public static MoveType getMoveType() {
        int choice = 0;
        printMenu();
        while(true) {
            try {
                choice = scanner.nextInt();
                if(choice != 1 && choice != 2 && choice != 3)
                    throw new IllegalArgumentException();
                break;                    
            } catch (Exception e) {
                scanner.nextLine();
                System.out.print("Podaj poprawny argument: ");
            }
        }
        if(choice == 1) {
            return MoveType.NORMAL_MOVE;
        } else if(choice == 2) {
            return MoveType.PASS;
        } else if(choice == 3) {
            return MoveType.PASS; 
        } else {
            return null;
        }       
    }

    public static Position getPositionFromPlayerInput(int boardSize) {
        return new Position(getXFromPlayerInput(boardSize) - 1, getYFromPlayerInput(boardSize) - 1);
    }

    public static int getXFromPlayerInput(int boardSize) {
        int x;
        System.out.print("Podaj x: "); 
        while(true) {
            try {
                x = scanner.nextInt();
                if(x < 0 || x > boardSize)
                    throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS);
                break;                    
            } catch (Exception e) {
                scanner.nextLine();
                System.out.print("Podaj poprawny x: ");
            }
        }
        return x;
    }

    public static int getYFromPlayerInput(int boardSize) {
        int y;
        System.out.print("Podaj y: "); 
        while(true) {
            try {
                y = scanner.nextInt();
                if(y < 0 || y > boardSize)
                    throw new InvalidMoveException(ErrorCode.OUT_OF_BOUNDS);
                break;                    
            } catch (Exception e) {
                scanner.nextLine();
                System.out.print("Podaj poprawny y: ");
            }
        }
        return y;
    }

    public static void printMenu() {
        System.out.print("Jaka akcje chcesz podjac?\n" 
                           + "1 - postaw kamien\n" 
                           + "2 - spasuj\n" 
                           + "3 - poddaj sie\n"
                           + "Wpisz odpowiednia cyfre: ");
    }

    public static String getPlayerName() {
        String playerName = null;
        System.out.print("Podaj nazwe gracza (minimum 3 znaki + brak spacji): ");
        while (true) {
            try {
                playerName = scanner.nextLine();
                if(playerName.length() < 3 || playerName.contains(" ")) {
                    throw new IllegalArgumentException();
                } 
                break;
            } catch (Exception e) {
                    System.out.print("Podaj poprawna nazwe gracza");
            }            
        }
        return playerName;
    }

    public static int getBoardSize() {
        int boardSize = 0;
        System.out.print("Wybierz rozmiar planszy na ktorej chcesz rozpoczac gre (dostępne 9, 13, 19): ");
        while (true) {
            try {
                boardSize = scanner.nextInt();
                if(boardSize != 9 && boardSize != 13 && boardSize != 19)
                    throw new IllegalArgumentException();
                System.out.println("boardsize : " + boardSize);
                break;
            } catch (Exception e) {
                System.out.print("Prosze wprowadzic prawidlowa wielkosc planszy");
            }
        }
        return boardSize;
    }

    public static void printGameStartingMessage(GameResponse game) {
        System.out.println("Znaleziono przeciwnika!");
        System.out.println("Id gry: " + game.id());
        System.out.println("Gracz czarny: " + game.blackPlayer().nickname());
        System.out.println("Gracz bialy: " + game.whitePlayer().nickname());
    }
}