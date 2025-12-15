package com.gogame;

public class BoardCharacters {
    // Linie
    public static final String HORIZONTAL = "───"; // Szersze, żeby kamień się zmieścił
    public static final String VERTICAL   = "│";
    public static final String VERTICAL_SPACER = "   ";

    // Skrzyżowania
    public static final String CROSS      = "┼";

    // Krawędzie (T-kształtne)
    public static final String T_DOWN     = "┬"; // Góra
    public static final String T_UP       = "┴"; // Dół
    public static final String T_RIGHT    = "├"; // Lewo
    public static final String T_LEFT     = "┤"; // Prawo

    // Narożniki
    public static final String TL_CORNER  = "┌"; // Top-Left
    public static final String TR_CORNER  = "┐"; // Top-Right
    public static final String BL_CORNER  = "└"; // Bottom-Left
    public static final String BR_CORNER  = "┘"; // Bottom-Right
    
    // Kamienie (opcjonalnie)
    public static final String STONE_BLACK = "○"; // Ze spacjami dla wyrównania
    public static final String STONE_WHITE = "●"; 
}