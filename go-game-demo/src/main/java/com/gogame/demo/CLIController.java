package com.gogame.demo;

import java.util.Scanner;

import com.gogame.demo.InvalidMoveException.ErrorCode;

public class CLIController {

    private static final Scanner scanner = new Scanner(System.in);

    public static int getX(int boardSize) {
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

    public static int getY(int boardSize) {
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
}