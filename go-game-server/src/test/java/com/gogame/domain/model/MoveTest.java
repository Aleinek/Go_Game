package com.gogame.domain.model;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void testMoveCreation() {
        Player player = new Player(UUID.randomUUID(), "Alice");
        Position position = new Position(3, 4);
        int moveNumber = 5;
        
        Move move = new Move(player, position, moveNumber);
        
        assertNotNull(move.id);
        assertEquals(player, move.player);
        assertEquals(position, move.position);
        assertEquals(moveNumber, move.moveNumber);
        assertFalse(move.isPass());
        assertEquals(0, move.capturedStones);
        assertNotNull(move.timestamp);
    }

    @Test
    void testPassMove() {
        Player player = new Player(UUID.randomUUID(), "Bob");
        int moveNumber = 10;
        
        Move move = Move.pass(player, moveNumber);
        
        assertNotNull(move.id);
        assertEquals(player, move.player);
        assertNull(move.position);
        assertEquals(moveNumber, move.moveNumber);
        assertTrue(move.isPass());
        assertNotNull(move.timestamp);
    }

    @Test
    void testMoveWithDifferentMoveNumbers() {
        Player player = new Player(UUID.randomUUID(), "Charlie");
        Position pos1 = new Position(0, 0);
        Position pos2 = new Position(1, 1);
        
        Move move1 = new Move(player, pos1, 1);
        Move move2 = new Move(player, pos2, 2);
        
        assertEquals(1, move1.moveNumber);
        assertEquals(2, move2.moveNumber);
        assertNotEquals(move1.id, move2.id);
    }

    @Test
    void testIsPassForRegularMove() {
        Player player = new Player(UUID.randomUUID(), "Dave");
        Position position = new Position(5, 5);
        
        Move move = new Move(player, position, 1);
        
        assertFalse(move.isPass());
    }

    @Test
    void testIsPassForPassMove() {
        Player player = new Player(UUID.randomUUID(), "Eve");
        
        Move move = Move.pass(player, 1);
        
        assertTrue(move.isPass());
    }
}
