package com.gogame.printer;

import com.gogame.model.Board;
import com.gogame.model.Stone;
import com.gogame.model.StoneColor;
import com.gogame.model.Territory;

public class BoardPrinter {

    public static void printBoard(Board board, int whitePrisoners, int blackPrisoners, int whiteTerritory, int blackTerritory) {
        int boardSize = board.getSize();
        int maxIdx = boardSize - 1;
        Stone[][] grid = board.getGrid();

        // Territory territory = board.getTerritory();

        System.out.println("\nTerytorium czarne: " + blackTerritory + ", Terytorium biale: " + whiteTerritory);
        System.out.println("Biali jency: " + whitePrisoners + ", Czarni jency: " + blackPrisoners);

        for(int y = 0; y < boardSize; y++) { 
            if(y == 0) {
                System.out.print("   ");
                for(int i = 0; i < boardSize; i++) {
                    int currColumn = i + 1;
                    if(i == maxIdx)
                        System.out.print(getCompactNumber(currColumn));
                    else 
                        System.out.print(getCompactNumber(currColumn) + BoardCharacters.VERTICAL_SPACER);
                }
                System.out.println();
            }

            for(int x = 0; x < boardSize; x++) { 

                Stone currStone = grid[x][y];
                if(currStone == null) {
                    if(y == 0) {
                        if(x == 0)
                            System.out.print(getCompactNumber(y + 1) + "  " + BoardCharacters.TL_CORNER + BoardCharacters.HORIZONTAL);
                        else if(x == maxIdx)
                            System.out.print(BoardCharacters.TR_CORNER);
                        else
                            System.out.print(BoardCharacters.T_DOWN + BoardCharacters.HORIZONTAL);
                    } else if(y == maxIdx) {
                        if(x == 0)
                            System.out.print(getCompactNumber(y + 1) + "  " + BoardCharacters.BL_CORNER + BoardCharacters.HORIZONTAL);
                        else if(x == maxIdx)
                            System.out.print(BoardCharacters.BR_CORNER);
                        else
                            System.out.print(BoardCharacters.T_UP + BoardCharacters.HORIZONTAL);
                    } else {
                        if(x == 0)
                            System.out.print(getCompactNumber(y + 1) + "  " + BoardCharacters.T_RIGHT + BoardCharacters.HORIZONTAL);
                        else if(x == maxIdx)
                            System.out.print(BoardCharacters.T_LEFT);
                        else
                            System.out.print(BoardCharacters.CROSS + BoardCharacters.HORIZONTAL);
                    }
                } else {
                    String stoneStr = (currStone.getColor() == StoneColor.WHITE) 
                      ? BoardCharacters.STONE_WHITE 
                      : BoardCharacters.STONE_BLACK;
                    
                    if(x == 0)
                        System.out.print(getCompactNumber(y + 1) + "  " + stoneStr + BoardCharacters.HORIZONTAL);
                    else if(x == maxIdx)
                        System.out.print(stoneStr);
                    else
                        System.out.print(stoneStr + BoardCharacters.HORIZONTAL);
                } 
            }
            System.out.println();

            if (y < maxIdx) {
                System.out.print("   "); 
            
                for (int x = 0; x < boardSize; x++) {
                    if (x == maxIdx) {
                        System.out.print(BoardCharacters.VERTICAL);
                    } else {
                        System.out.print(BoardCharacters.VERTICAL + BoardCharacters.VERTICAL_SPACER);
                    }
                }
                System.out.println();
            }
        }
    }

    public static String getCompactNumber(int number) {
        if (number >= 1 && number <= 20) {
            // Kod Unicode dla ① to 0x2460.
            // Dodajemy (number - 1) aby uzyskać odpowiedni znak.
            char c = (char) (0x2460 + (number - 1));
            return String.valueOf(c);
        }
        // Obsługa liczb > 20 (opcjonalnie zwykły tekst)
        return String.valueOf(number);
    }
}
