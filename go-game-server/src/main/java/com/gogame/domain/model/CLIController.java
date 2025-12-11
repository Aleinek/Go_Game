package com.gogame.domain.model;

import java.util.Scanner;

import com.gogame.domain.enums.MoveType;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.exception.InvalidMoveException.ErrorCode;

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
}