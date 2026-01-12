package com.gogame.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidMoveException;

public class BoardTest {

    private Board board;
    private Player blackPlayer;
    private Player whitePlayer;

    @BeforeEach
    public void setUp() {
        blackPlayer = new Player(UUID.randomUUID(), "Black", StoneColor.BLACK);
        whitePlayer = new Player(UUID.randomUUID(), "White", StoneColor.WHITE);
        board = new Board(9, blackPlayer, whitePlayer); // 9x9 is enough
    }

    @Test
    public void testKoRule() {
        // Create a Ko situation
        //   0 1 2 3
        // 0 . B W .
        // 1 B W . W
        // 2 . B W .
        
        // White stones setup
        board.placeStone(new Position(0, 2), StoneColor.WHITE);
        board.placeStone(new Position(1, 3), StoneColor.WHITE);
        board.placeStone(new Position(2, 2), StoneColor.WHITE);
        board.placeStone(new Position(1, 1), StoneColor.WHITE); // Victim

        // Black stones setup
        board.placeStone(new Position(0, 1), StoneColor.BLACK);
        board.placeStone(new Position(1, 0), StoneColor.BLACK);
        board.placeStone(new Position(2, 1), StoneColor.BLACK);

        // Action: Black plays at (1, 2)
        // This should capture White at (1, 1)
        Position playPosition = new Position(1, 2);
        board.placeStone(playPosition, StoneColor.BLACK);

        // Verify capture happened
        assertNull(board.getStoneAt(new Position(1, 1)), "Stone at (1,1) should be captured");

        // Action: White tries to retake immediately at (1, 1)
        Position koPosition = new Position(1, 1);
        InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
            board.placeStone(koPosition, StoneColor.WHITE);
        });

        assertEquals(InvalidMoveException.ErrorCode.KO_VIOLATION, exception.getErrorCode());

        // Action: White plays elsewhere (Resolve Ko)
        board.placeStone(new Position(5, 5), StoneColor.WHITE);

        // Action: Black responds elsewhere
        board.placeStone(new Position(5, 6), StoneColor.BLACK);

        // Action: White retakes at (1, 1) - Should be allowed now
        assertDoesNotThrow(() -> {
            board.placeStone(koPosition, StoneColor.WHITE);
        });
    }
}
