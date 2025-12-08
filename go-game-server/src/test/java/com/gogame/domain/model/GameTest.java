package com.gogame.domain.model;

import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Player blackPlayer;
    private Player whitePlayer;
    private Game game;

    @BeforeEach
    void setUp() {
        blackPlayer = new Player(UUID.randomUUID(), "Black Player");
        whitePlayer = new Player(UUID.randomUUID(), "White Player");
        game = new Game(blackPlayer, whitePlayer, 9);
    }

    @Test
    void testGameCreation() {
        assertNotNull(game.id);
        assertNotNull(game.board);
        assertEquals(blackPlayer, game.blackPlayer);
        assertEquals(whitePlayer, game.whitePlayer);
        assertEquals(StoneColor.BLACK, game.currentTurn);
        assertEquals(GameStatus.IN_PROGRESS, game.status);
        assertTrue(game.moves.isEmpty());
        assertEquals(0, game.consecutivePasses);
    }

    @Test
    void testMakeValidMove() {
        Position position = new Position(3, 3);
        
        MoveResult result = game.makeMove(position);
        
        assertTrue(result.getSuccess());
        assertEquals(1, game.moves.size());
        assertEquals(StoneColor.WHITE, game.currentTurn);
        assertEquals(0, game.consecutivePasses);
    }

    @Test
    void testMakeMultipleMoves() {
        game.makeMove(new Position(2, 2));
        game.makeMove(new Position(2, 3));
        game.makeMove(new Position(3, 2));
        
        assertEquals(3, game.moves.size());
        assertEquals(StoneColor.WHITE, game.currentTurn);
    }

    @Test
    void testPass() {
        MoveResult result = game.pass();
        
        assertTrue(result.getSuccess());
        assertEquals(1, game.moves.size());
        assertEquals(1, game.consecutivePasses);
        assertEquals(StoneColor.WHITE, game.currentTurn);
        assertTrue(game.moves.get(0).isPass());
    }

    @Test
    void testTwoConsecutivePassesEndsGame() {
        game.pass();
        assertEquals(GameStatus.IN_PROGRESS, game.status);
        
        game.pass();
        assertEquals(GameStatus.FINISHED, game.status);
        assertTrue(game.isGameOver());
    }

    @Test
    void testPassResetsAfterRegularMove() {
        game.pass();
        assertEquals(1, game.consecutivePasses);
        
        game.makeMove(new Position(3, 3));
        assertEquals(0, game.consecutivePasses);
    }

    @Test
    void testResign() {
        game.resign(blackPlayer);
        
        assertEquals(GameStatus.RESIGNED, game.status);
        assertTrue(game.isGameOver());
    }

    @Test
    void testGetWinnerAfterResign() {
        game.resign(blackPlayer);
        
        Player winner = game.getWinner();
        assertEquals(whitePlayer, winner);
    }

    @Test
    void testGetCurrentPlayer() {
        assertEquals(blackPlayer, game.getCurrentPlayer());
        
        game.makeMove(new Position(0, 0));
        assertEquals(whitePlayer, game.getCurrentPlayer());
        
        game.makeMove(new Position(1, 1));
        assertEquals(blackPlayer, game.getCurrentPlayer());
    }

    @Test
    void testIsGameOverWhenInProgress() {
        assertFalse(game.isGameOver());
    }

    @Test
    void testIsGameOverWhenFinished() {
        game.pass();
        game.pass();
        
        assertTrue(game.isGameOver());
    }

    @Test
    void testIsGameOverWhenResigned() {
        game.resign(blackPlayer);
        
        assertTrue(game.isGameOver());
    }

    @Test
    void testCannotMoveAfterGameEnds() {
        game.pass();
        game.pass();
        
        MoveResult result = game.makeMove(new Position(3, 3));
        
        assertFalse(result.getSuccess());
    }

    @Test
    void testSwitchTurn() {
        assertEquals(StoneColor.BLACK, game.currentTurn);
        
        game.switchTurn();
        assertEquals(StoneColor.WHITE, game.currentTurn);
        
        game.switchTurn();
        assertEquals(StoneColor.BLACK, game.currentTurn);
    }

    @Test
    void testMoveNumberIncreases() {
        game.makeMove(new Position(0, 0));
        game.makeMove(new Position(1, 1));
        game.makeMove(new Position(2, 2));
        
        assertEquals(1, game.moves.get(0).moveNumber);
        assertEquals(2, game.moves.get(1).moveNumber);
        assertEquals(3, game.moves.get(2).moveNumber);
    }

    @Test
    void testGetWinnerWhenGameInProgress() {
        assertNull(game.getWinner());
    }

    @Test
    void testGetWinnerWhenGameFinishedByPasses() {
        game.pass();
        game.pass();
        
        assertNull(game.getWinner());
    }
}
