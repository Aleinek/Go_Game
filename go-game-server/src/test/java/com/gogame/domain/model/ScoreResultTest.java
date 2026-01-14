package com.gogame.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScoreResult Tests")
class ScoreResultTest {

    @Test
    @DisplayName("should calculate black winning with higher territory")
    void shouldCalculateBlackWinningWithHigherTerritory() {
        ScoreResult score = new ScoreResult(
            50,  // blackTerritory
            30,  // whiteTerritory
            5,   // blackPrisoners
            3,   // whitePrisoners
            0,   // blackDeadStones
            2,   // whiteDeadStones
            6.5  // komi
        );
        
        // Black: 50 + 5 + 2 = 57
        // White: 30 + 3 + 0 + 6.5 = 39.5
        assertEquals(57.0, score.getBlackTotal());
        assertEquals(39.5, score.getWhiteTotal());
        assertEquals("BLACK", score.getWinner());
        assertEquals(17.5, score.getScoreDifference());
    }

    @Test
    @DisplayName("should calculate white winning with komi advantage")
    void shouldCalculateWhiteWinningWithKomiAdvantage() {
        ScoreResult score = new ScoreResult(
            30,  // blackTerritory
            30,  // whiteTerritory
            5,   // blackPrisoners
            5,   // whitePrisoners
            0,   // blackDeadStones
            0,   // whiteDeadStones
            6.5  // komi
        );
        
        // Black: 30 + 5 + 0 = 35
        // White: 30 + 5 + 0 + 6.5 = 41.5
        assertEquals(35.0, score.getBlackTotal());
        assertEquals(41.5, score.getWhiteTotal());
        assertEquals("WHITE", score.getWinner());
    }

    @Test
    @DisplayName("should count dead stones as prisoners for opponent")
    void shouldCountDeadStonesAsPrisonersForOpponent() {
        ScoreResult score = new ScoreResult(
            20,  // blackTerritory
            20,  // whiteTerritory
            0,   // blackPrisoners
            0,   // whitePrisoners
            10,  // blackDeadStones - these count for white
            5,   // whiteDeadStones - these count for black
            6.5  // komi
        );
        
        // Black: 20 + 0 + 5 = 25
        // White: 20 + 0 + 10 + 6.5 = 36.5
        assertEquals(25.0, score.getBlackTotal());
        assertEquals(36.5, score.getWhiteTotal());
        assertEquals("WHITE", score.getWinner());
    }

    @Test
    @DisplayName("should handle tie scenario")
    void shouldHandleTieScenario() {
        // Create a theoretical tie (unlikely with 0.5 komi but testing the logic)
        ScoreResult score = new ScoreResult(
            10,   // blackTerritory
            10,   // whiteTerritory
            0,    // blackPrisoners
            0,    // whitePrisoners
            0,    // blackDeadStones
            0,    // whiteDeadStones
            0.0   // no komi for this test
        );
        
        assertEquals(10.0, score.getBlackTotal());
        assertEquals(10.0, score.getWhiteTotal());
        assertEquals("TIE", score.getWinner());
        assertEquals(0.0, score.getScoreDifference());
    }

    @Test
    @DisplayName("should have correct toString format")
    void shouldHaveCorrectToStringFormat() {
        ScoreResult score = new ScoreResult(40, 35, 5, 3, 2, 4, 6.5);
        String result = score.toString();
        
        assertTrue(result.contains("Black"));
        assertTrue(result.contains("White"));
        assertTrue(result.contains("Winner"));
    }
}
