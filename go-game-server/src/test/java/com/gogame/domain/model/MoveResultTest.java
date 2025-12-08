package com.gogame.domain.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MoveResultTest {

    @Test
    void testMoveResultSuccess() {
        MoveResult result = new MoveResult(true);
        
        assertTrue(result.getSuccess());
    }

    @Test
    void testMoveResultFailure() {
        MoveResult result = new MoveResult(false);
        
        assertFalse(result.getSuccess());
    }

    @Test
    void testGetCapturedStonesWhenNull() {
        MoveResult result = new MoveResult(true);
        
        assertNull(result.getCapturedStones());
    }

    @Test
    void testGetCapturedStonesWhenEmpty() {
        MoveResult result = new MoveResult(true);
        result.capturedStones = new ArrayList<>();
        
        assertNotNull(result.getCapturedStones());
        assertTrue(result.getCapturedStones().isEmpty());
    }

    @Test
    void testGetCapturedStonesWithStones() {
        MoveResult result = new MoveResult(true);
        List<Stone> stones = new ArrayList<>();
        stones.add(new Stone(new Position(0, 0), com.gogame.domain.enums.StoneColor.BLACK));
        stones.add(new Stone(new Position(0, 1), com.gogame.domain.enums.StoneColor.BLACK));
        result.capturedStones = stones;
        
        assertEquals(2, result.getCapturedStones().size());
    }
}
