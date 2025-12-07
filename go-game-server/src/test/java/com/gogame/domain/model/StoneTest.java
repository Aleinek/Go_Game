package com.gogame.domain.model;

import com.gogame.domain.enums.StoneColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stone Tests")
class StoneTest {

    @Nested
    @DisplayName("Constructor and Getters")
    class ConstructorTests {

        @Test
        @DisplayName("should create black stone with correct position")
        void shouldCreateBlackStoneWithCorrectPosition() {
            Position position = new Position(3, 5);
            Stone stone = new Stone(position, StoneColor.BLACK);
            
            assertEquals(position, stone.getPosition());
            assertEquals(StoneColor.BLACK, stone.getColor());
        }

        @Test
        @DisplayName("should create white stone with correct position")
        void shouldCreateWhiteStoneWithCorrectPosition() {
            Position position = new Position(10, 10);
            Stone stone = new Stone(position, StoneColor.WHITE);
            
            assertEquals(position, stone.getPosition());
            assertEquals(StoneColor.WHITE, stone.getColor());
        }

        @Test
        @DisplayName("should create stone at corner position (0,0)")
        void shouldCreateStoneAtCornerPosition() {
            Position position = new Position(0, 0);
            Stone stone = new Stone(position, StoneColor.BLACK);
            
            assertEquals(0, stone.getPosition().getX());
            assertEquals(0, stone.getPosition().getY());
        }

        @Test
        @DisplayName("should create stone with EMPTY color")
        void shouldCreateStoneWithEmptyColor() {
            Position position = new Position(5, 5);
            Stone stone = new Stone(position, StoneColor.EMPTY);
            
            assertEquals(StoneColor.EMPTY, stone.getColor());
        }
    }

    @Nested
    @DisplayName("Position Retrieval")
    class PositionRetrievalTests {

        @Test
        @DisplayName("should return same position object")
        void shouldReturnSamePositionObject() {
            Position position = new Position(3, 5);
            Stone stone = new Stone(position, StoneColor.BLACK);
            
            assertSame(position, stone.getPosition());
        }

        @Test
        @DisplayName("position should have correct x coordinate")
        void positionShouldHaveCorrectXCoordinate() {
            Stone stone = new Stone(new Position(7, 3), StoneColor.WHITE);
            
            assertEquals(7, stone.getPosition().getX());
        }

        @Test
        @DisplayName("position should have correct y coordinate")
        void positionShouldHaveCorrectYCoordinate() {
            Stone stone = new Stone(new Position(7, 3), StoneColor.WHITE);
            
            assertEquals(3, stone.getPosition().getY());
        }
    }

    @Nested
    @DisplayName("Color Tests")
    class ColorTests {

        @Test
        @DisplayName("black stone should have BLACK color")
        void blackStoneShouldHaveBlackColor() {
            Stone stone = new Stone(new Position(0, 0), StoneColor.BLACK);
            
            assertEquals(StoneColor.BLACK, stone.getColor());
        }

        @Test
        @DisplayName("white stone should have WHITE color")
        void whiteStoneShouldHaveWhiteColor() {
            Stone stone = new Stone(new Position(0, 0), StoneColor.WHITE);
            
            assertEquals(StoneColor.WHITE, stone.getColor());
        }

        @Test
        @DisplayName("should distinguish between black and white stones")
        void shouldDistinguishBetweenBlackAndWhiteStones() {
            Position position = new Position(5, 5);
            Stone blackStone = new Stone(position, StoneColor.BLACK);
            Stone whiteStone = new Stone(position, StoneColor.WHITE);
            
            assertNotEquals(blackStone.getColor(), whiteStone.getColor());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should create multiple stones at different positions")
        void shouldCreateMultipleStonesAtDifferentPositions() {
            Stone stone1 = new Stone(new Position(0, 0), StoneColor.BLACK);
            Stone stone2 = new Stone(new Position(18, 18), StoneColor.WHITE);
            Stone stone3 = new Stone(new Position(9, 9), StoneColor.BLACK);
            
            assertNotEquals(stone1.getPosition(), stone2.getPosition());
            assertNotEquals(stone1.getPosition(), stone3.getPosition());
            assertNotEquals(stone2.getPosition(), stone3.getPosition());
        }

        @Test
        @DisplayName("should create stones at max board position for 19x19")
        void shouldCreateStonesAtMaxBoardPosition() {
            Position maxPosition = new Position(18, 18);
            Stone stone = new Stone(maxPosition, StoneColor.BLACK);
            
            assertEquals(18, stone.getPosition().getX());
            assertEquals(18, stone.getPosition().getY());
        }
    }
}
