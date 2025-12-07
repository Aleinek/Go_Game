package com.gogame.domain.model;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.exception.InvalidMoveException.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board Tests")
class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(9);
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create 9x9 board")
        void shouldCreate9x9Board() {
            Board board9 = new Board(9);
            assertEquals(9, board9.getSize());
        }

        @Test
        @DisplayName("should create 13x13 board")
        void shouldCreate13x13Board() {
            Board board13 = new Board(13);
            assertEquals(13, board13.getSize());
        }

        @Test
        @DisplayName("should create 19x19 board")
        void shouldCreate19x19Board() {
            Board board19 = new Board(19);
            assertEquals(19, board19.getSize());
        }

        @Test
        @DisplayName("should create empty board")
        void shouldCreateEmptyBoard() {
            Board newBoard = new Board(9);
            
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 9; y++) {
                    assertNull(newBoard.getStoneAt(new Position(x, y)));
                }
            }
        }
    }

    @Nested
    @DisplayName("getStoneAt()")
    class GetStoneAtTests {

        @Test
        @DisplayName("should return null for empty position")
        void shouldReturnNullForEmptyPosition() {
            assertNull(board.getStoneAt(new Position(4, 4)));
        }

        @Test
        @DisplayName("should return stone after placement")
        void shouldReturnStoneAfterPlacement() {
            Position pos = new Position(3, 3);
            board.placeStone(pos, StoneColor.BLACK);
            
            Stone retrieved = board.getStoneAt(pos);
            
            assertNotNull(retrieved);
            assertEquals(StoneColor.BLACK, retrieved.getColor());
        }

        @Test
        @DisplayName("should throw exception for out of bounds position")
        void shouldThrowExceptionForOutOfBoundsPosition() {
            InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
                board.getStoneAt(new Position(10, 5));
            });
            assertEquals(ErrorCode.OUT_OF_BOUNDS, exception.getErrorCode());
        }

        @Test
        @DisplayName("should throw exception for negative position")
        void shouldThrowExceptionForNegativePosition() {
            InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
                board.getStoneAt(new Position(-1, 5));
            });
            assertEquals(ErrorCode.OUT_OF_BOUNDS, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("placeStone()")
    class PlaceStoneTests {

        @Test
        @DisplayName("should place stone on empty position")
        void shouldPlaceStoneOnEmptyPosition() {
            Position pos = new Position(4, 4);
            
            board.placeStone(pos, StoneColor.BLACK);
            
            assertNotNull(board.getStoneAt(pos));
            assertEquals(StoneColor.BLACK, board.getStoneAt(pos).getColor());
        }

        @Test
        @DisplayName("should throw exception when placing stone on occupied position")
        void shouldThrowExceptionWhenPlacingStoneOnOccupiedPosition() {
            Position pos = new Position(4, 4);
            board.placeStone(pos, StoneColor.BLACK);
            
            InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
                board.placeStone(pos, StoneColor.WHITE);
            });
            assertEquals(ErrorCode.POSITION_OCCUPIED, exception.getErrorCode());
            assertEquals(pos, exception.getPosition());
        }

        @Test
        @DisplayName("should throw exception for out of bounds position")
        void shouldThrowExceptionForOutOfBoundsPosition() {
            InvalidMoveException exception = assertThrows(InvalidMoveException.class, () -> {
                board.placeStone(new Position(10, 5), StoneColor.BLACK);
            });
            assertEquals(ErrorCode.POSITION_OCCUPIED, exception.getErrorCode());
        }

        @Test
        @DisplayName("should allow placing stones at corner positions")
        void shouldAllowPlacingStonesAtCornerPositions() {
            board.placeStone(new Position(0, 0), StoneColor.BLACK);
            board.placeStone(new Position(8, 0), StoneColor.WHITE);
            board.placeStone(new Position(0, 8), StoneColor.BLACK);
            board.placeStone(new Position(8, 8), StoneColor.WHITE);
            
            assertNotNull(board.getStoneAt(new Position(0, 0)));
            assertNotNull(board.getStoneAt(new Position(8, 0)));
            assertNotNull(board.getStoneAt(new Position(0, 8)));
            assertNotNull(board.getStoneAt(new Position(8, 8)));
        }

        @Test
        @DisplayName("should allow placing black and white stones")
        void shouldAllowPlacingBlackAndWhiteStones() {
            board.placeStone(new Position(3, 3), StoneColor.BLACK);
            board.placeStone(new Position(4, 4), StoneColor.WHITE);
            
            assertEquals(StoneColor.BLACK, board.getStoneAt(new Position(3, 3)).getColor());
            assertEquals(StoneColor.WHITE, board.getStoneAt(new Position(4, 4)).getColor());
        }
    }

    @Nested
    @DisplayName("isValidMove()")
    class IsValidMoveTests {

        @Test
        @DisplayName("should return true for empty valid position")
        void shouldReturnTrueForEmptyValidPosition() {
            assertTrue(board.isValidMove(new Position(4, 4), StoneColor.BLACK));
        }

        @Test
        @DisplayName("should return false for occupied position")
        void shouldReturnFalseForOccupiedPosition() {
            Position pos = new Position(4, 4);
            board.placeStone(pos, StoneColor.BLACK);
            
            assertFalse(board.isValidMove(pos, StoneColor.WHITE));
        }

        @Test
        @DisplayName("should return false for out of bounds position")
        void shouldReturnFalseForOutOfBoundsPosition() {
            assertFalse(board.isValidMove(new Position(10, 5), StoneColor.BLACK));
        }

        @Test
        @DisplayName("should return false for negative position")
        void shouldReturnFalseForNegativePosition() {
            assertFalse(board.isValidMove(new Position(-1, 5), StoneColor.BLACK));
        }

        @Test
        @DisplayName("should return true for all corner positions on empty board")
        void shouldReturnTrueForAllCornerPositionsOnEmptyBoard() {
            assertTrue(board.isValidMove(new Position(0, 0), StoneColor.BLACK));
            assertTrue(board.isValidMove(new Position(8, 0), StoneColor.BLACK));
            assertTrue(board.isValidMove(new Position(0, 8), StoneColor.BLACK));
            assertTrue(board.isValidMove(new Position(8, 8), StoneColor.BLACK));
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmptyTests {

        @Test
        @DisplayName("should return true for empty position")
        void shouldReturnTrueForEmptyPosition() {
            assertTrue(board.isEmpty(new Position(4, 4)));
        }

        @Test
        @DisplayName("should return false for occupied position")
        void shouldReturnFalseForOccupiedPosition() {
            Position pos = new Position(4, 4);
            board.placeStone(pos, StoneColor.BLACK);
            
            assertFalse(board.isEmpty(pos));
        }

        @Test
        @DisplayName("should return true for all positions on new board")
        void shouldReturnTrueForAllPositionsOnNewBoard() {
            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 9; y++) {
                    assertTrue(board.isEmpty(new Position(x, y)));
                }
            }
        }
    }

    @Nested
    @DisplayName("copy()")
    class CopyTests {

        @Test
        @DisplayName("should create a new board instance")
        void shouldCreateNewBoardInstance() {
            Board copy = board.copy();
            
            assertNotSame(board, copy);
        }

        @Test
        @DisplayName("should copy board size")
        void shouldCopyBoardSize() {
            Board copy = board.copy();
            
            assertEquals(board.getSize(), copy.getSize());
        }

        @Test
        @DisplayName("should copy stone positions")
        void shouldCopyStonePositions() {
            board.placeStone(new Position(3, 3), StoneColor.BLACK);
            board.placeStone(new Position(4, 4), StoneColor.WHITE);
            
            Board copy = board.copy();
            
            assertNotNull(copy.getStoneAt(new Position(3, 3)));
            assertNotNull(copy.getStoneAt(new Position(4, 4)));
            assertEquals(StoneColor.BLACK, copy.getStoneAt(new Position(3, 3)).getColor());
            assertEquals(StoneColor.WHITE, copy.getStoneAt(new Position(4, 4)).getColor());
        }

        @Test
        @DisplayName("modifications to copy should not affect original")
        void modificationsToCopyShouldNotAffectOriginal() {
            Board copy = board.copy();
            copy.placeStone(new Position(5, 5), StoneColor.BLACK);
            
            assertTrue(board.isEmpty(new Position(5, 5)));
        }

        @Test
        @DisplayName("modifications to original should not affect copy")
        void modificationsToOriginalShouldNotAffectCopy() {
            Board copy = board.copy();
            board.placeStone(new Position(5, 5), StoneColor.BLACK);
            
            assertTrue(copy.isEmpty(new Position(5, 5)));
        }
    }

    @Nested
    @DisplayName("Board Sizes")
    class BoardSizeTests {

        @Test
        @DisplayName("9x9 board should have correct boundaries")
        void board9x9ShouldHaveCorrectBoundaries() {
            Board board9 = new Board(9);
            
            assertTrue(board9.isValidMove(new Position(0, 0), StoneColor.BLACK));
            assertTrue(board9.isValidMove(new Position(8, 8), StoneColor.BLACK));
            assertFalse(board9.isValidMove(new Position(9, 0), StoneColor.BLACK));
            assertFalse(board9.isValidMove(new Position(0, 9), StoneColor.BLACK));
        }

        @Test
        @DisplayName("13x13 board should have correct boundaries")
        void board13x13ShouldHaveCorrectBoundaries() {
            Board board13 = new Board(13);
            
            assertTrue(board13.isValidMove(new Position(0, 0), StoneColor.BLACK));
            assertTrue(board13.isValidMove(new Position(12, 12), StoneColor.BLACK));
            assertFalse(board13.isValidMove(new Position(13, 0), StoneColor.BLACK));
            assertFalse(board13.isValidMove(new Position(0, 13), StoneColor.BLACK));
        }

        @Test
        @DisplayName("19x19 board should have correct boundaries")
        void board19x19ShouldHaveCorrectBoundaries() {
            Board board19 = new Board(19);
            
            assertTrue(board19.isValidMove(new Position(0, 0), StoneColor.BLACK));
            assertTrue(board19.isValidMove(new Position(18, 18), StoneColor.BLACK));
            assertFalse(board19.isValidMove(new Position(19, 0), StoneColor.BLACK));
            assertFalse(board19.isValidMove(new Position(0, 19), StoneColor.BLACK));
        }
    }

    @Nested
    @DisplayName("Game Scenarios")
    class GameScenarioTests {

        @Test
        @DisplayName("should allow typical opening moves")
        void shouldAllowTypicalOpeningMoves() {
            Board board19 = new Board(19);
            
            // Star points on 19x19 board (hoshi)
            board19.placeStone(new Position(3, 3), StoneColor.BLACK);
            board19.placeStone(new Position(15, 15), StoneColor.WHITE);
            board19.placeStone(new Position(3, 15), StoneColor.BLACK);
            board19.placeStone(new Position(15, 3), StoneColor.WHITE);
            
            assertNotNull(board19.getStoneAt(new Position(3, 3)));
            assertNotNull(board19.getStoneAt(new Position(15, 15)));
            assertNotNull(board19.getStoneAt(new Position(3, 15)));
            assertNotNull(board19.getStoneAt(new Position(15, 3)));
        }

        @Test
        @DisplayName("should handle stones placed in sequence")
        void shouldHandleStonesPlacedInSequence() {
            // Simulate a few moves
            board.placeStone(new Position(2, 2), StoneColor.BLACK);
            board.placeStone(new Position(6, 6), StoneColor.WHITE);
            board.placeStone(new Position(2, 6), StoneColor.BLACK);
            board.placeStone(new Position(6, 2), StoneColor.WHITE);
            board.placeStone(new Position(4, 4), StoneColor.BLACK);
            
            assertEquals(StoneColor.BLACK, board.getStoneAt(new Position(2, 2)).getColor());
            assertEquals(StoneColor.WHITE, board.getStoneAt(new Position(6, 6)).getColor());
            assertEquals(StoneColor.BLACK, board.getStoneAt(new Position(2, 6)).getColor());
            assertEquals(StoneColor.WHITE, board.getStoneAt(new Position(6, 2)).getColor());
            assertEquals(StoneColor.BLACK, board.getStoneAt(new Position(4, 4)).getColor());
        }

        @Test
        @DisplayName("should handle edge play")
        void shouldHandleEdgePlay() {
            // Place stones along the edge
            for (int i = 0; i < 9; i++) {
                StoneColor color = i % 2 == 0 ? StoneColor.BLACK : StoneColor.WHITE;
                board.placeStone(new Position(i, 0), color);
            }
            
            for (int i = 0; i < 9; i++) {
                assertNotNull(board.getStoneAt(new Position(i, 0)));
            }
        }
    }
}
