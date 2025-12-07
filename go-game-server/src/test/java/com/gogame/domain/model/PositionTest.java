package com.gogame.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Position Tests")
class PositionTest {

    @Nested
    @DisplayName("Constructor and Getters")
    class ConstructorTests {

        @Test
        @DisplayName("should create position with correct coordinates")
        void shouldCreatePositionWithCorrectCoordinates() {
            Position position = new Position(3, 5);
            
            assertEquals(3, position.getX());
            assertEquals(5, position.getY());
        }

        @Test
        @DisplayName("should create position with zero coordinates")
        void shouldCreatePositionWithZeroCoordinates() {
            Position position = new Position(0, 0);
            
            assertEquals(0, position.getX());
            assertEquals(0, position.getY());
        }

        @Test
        @DisplayName("should create position with negative coordinates")
        void shouldCreatePositionWithNegativeCoordinates() {
            Position position = new Position(-1, -5);
            
            assertEquals(-1, position.getX());
            assertEquals(-5, position.getY());
        }
    }

    @Nested
    @DisplayName("getNeighbors()")
    class GetNeighborsTests {

        @Test
        @DisplayName("should return 4 neighbors for any position")
        void shouldReturnFourNeighbors() {
            Position position = new Position(5, 5);
            
            List<Position> neighbors = position.getNeighbors();
            
            assertEquals(4, neighbors.size());
        }

        @Test
        @DisplayName("should return correct neighbors for center position")
        void shouldReturnCorrectNeighborsForCenterPosition() {
            Position position = new Position(5, 5);
            
            List<Position> neighbors = position.getNeighbors();
            
            assertTrue(neighbors.contains(new Position(6, 5))); // right
            assertTrue(neighbors.contains(new Position(4, 5))); // left
            assertTrue(neighbors.contains(new Position(5, 6))); // up
            assertTrue(neighbors.contains(new Position(5, 4))); // down
        }

        @Test
        @DisplayName("should return correct neighbors for corner position (0,0)")
        void shouldReturnCorrectNeighborsForCornerPosition() {
            Position position = new Position(0, 0);
            
            List<Position> neighbors = position.getNeighbors();
            
            assertTrue(neighbors.contains(new Position(1, 0)));
            assertTrue(neighbors.contains(new Position(-1, 0)));
            assertTrue(neighbors.contains(new Position(0, 1)));
            assertTrue(neighbors.contains(new Position(0, -1)));
        }
    }

    @Nested
    @DisplayName("isValid()")
    class IsValidTests {

        @Test
        @DisplayName("should return true for valid position on 9x9 board")
        void shouldReturnTrueForValidPositionOn9x9Board() {
            Position position = new Position(4, 4);
            
            assertTrue(position.isValid(9));
        }

        @Test
        @DisplayName("should return true for corner position (0,0)")
        void shouldReturnTrueForCornerPosition() {
            Position position = new Position(0, 0);
            
            assertTrue(position.isValid(9));
        }

        @Test
        @DisplayName("should return true for max corner position (8,8) on 9x9 board")
        void shouldReturnTrueForMaxCornerPosition() {
            Position position = new Position(8, 8);
            
            assertTrue(position.isValid(9));
        }

        @Test
        @DisplayName("should return false for negative x coordinate")
        void shouldReturnFalseForNegativeX() {
            Position position = new Position(-1, 5);
            
            assertFalse(position.isValid(9));
        }

        @Test
        @DisplayName("should return false for negative y coordinate")
        void shouldReturnFalseForNegativeY() {
            Position position = new Position(5, -1);
            
            assertFalse(position.isValid(9));
        }

        @Test
        @DisplayName("should return false for x equal to board size")
        void shouldReturnFalseForXEqualToBoardSize() {
            Position position = new Position(9, 5);
            
            assertFalse(position.isValid(9));
        }

        @Test
        @DisplayName("should return false for y equal to board size")
        void shouldReturnFalseForYEqualToBoardSize() {
            Position position = new Position(5, 9);
            
            assertFalse(position.isValid(9));
        }

        @Test
        @DisplayName("should return false for x greater than board size")
        void shouldReturnFalseForXGreaterThanBoardSize() {
            Position position = new Position(15, 5);
            
            assertFalse(position.isValid(9));
        }

        @Test
        @DisplayName("should work correctly for 19x19 board")
        void shouldWorkCorrectlyFor19x19Board() {
            assertTrue(new Position(0, 0).isValid(19));
            assertTrue(new Position(18, 18).isValid(19));
            assertFalse(new Position(19, 0).isValid(19));
            assertFalse(new Position(0, 19).isValid(19));
        }

        @Test
        @DisplayName("should work correctly for 13x13 board")
        void shouldWorkCorrectlyFor13x13Board() {
            assertTrue(new Position(12, 12).isValid(13));
            assertFalse(new Position(13, 0).isValid(13));
        }
    }

    @Nested
    @DisplayName("equals() and hashCode()")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("should be equal for same coordinates")
        void shouldBeEqualForSameCoordinates() {
            Position pos1 = new Position(3, 5);
            Position pos2 = new Position(3, 5);
            
            assertEquals(pos1, pos2);
        }

        @Test
        @DisplayName("should not be equal for different x coordinates")
        void shouldNotBeEqualForDifferentXCoordinates() {
            Position pos1 = new Position(3, 5);
            Position pos2 = new Position(4, 5);
            
            assertNotEquals(pos1, pos2);
        }

        @Test
        @DisplayName("should not be equal for different y coordinates")
        void shouldNotBeEqualForDifferentYCoordinates() {
            Position pos1 = new Position(3, 5);
            Position pos2 = new Position(3, 6);
            
            assertNotEquals(pos1, pos2);
        }

        @Test
        @DisplayName("should return same hashCode for equal positions")
        void shouldReturnSameHashCodeForEqualPositions() {
            Position pos1 = new Position(3, 5);
            Position pos2 = new Position(3, 5);
            
            assertEquals(pos1.hashCode(), pos2.hashCode());
        }

        @Test
        @DisplayName("should be equal to itself")
        void shouldBeEqualToItself() {
            Position position = new Position(3, 5);
            
            assertEquals(position, position);
        }

        @Test
        @DisplayName("should not be equal to null")
        void shouldNotBeEqualToNull() {
            Position position = new Position(3, 5);
            
            assertNotEquals(null, position);
        }

        @Test
        @DisplayName("should not be equal to object of different type")
        void shouldNotBeEqualToObjectOfDifferentType() {
            Position position = new Position(3, 5);
            
            assertNotEquals("(3, 5)", position);
        }
    }
}
