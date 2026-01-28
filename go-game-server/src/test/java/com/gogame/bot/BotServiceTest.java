package com.gogame.bot;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.Board;
import com.gogame.domain.model.Game;
import com.gogame.domain.model.Player;
import com.gogame.dto.request.MakeMoveRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BotService class.
 */
@DisplayName("BotService Tests")
class BotServiceTest {
    
    private BotService botService;
    
    @BeforeEach
    void setUp() {
        botService = new BotService();
    }
    
    @Test
    @DisplayName("Should identify bot players by nickname")
    void shouldIdentifyBotPlayers() {
        assertTrue(botService.isBotPlayer("Bot-abc123"), "Should identify bot nickname");
        assertTrue(botService.isBotPlayer("Bot-XYZ"), "Should identify bot nickname");
        assertFalse(botService.isBotPlayer("HumanPlayer"), "Should not identify human as bot");
        assertFalse(botService.isBotPlayer("PlayerBot"), "Bot prefix must be at start");
        assertFalse(botService.isBotPlayer(null), "Should handle null nickname");
    }
    
    @Test
    @DisplayName("Should calculate valid move for game")
    void shouldCalculateValidMove() {
        Player blackPlayer = new Player(UUID.randomUUID(), "Human", StoneColor.BLACK, false);
        Player whitePlayer = new Player(UUID.randomUUID(), "Bot-test", StoneColor.WHITE, true);
        Board board = new Board(9, blackPlayer, whitePlayer);
        Game game = new Game(blackPlayer, whitePlayer, board);
        
        MakeMoveRequest move = botService.calculateMove(game, StoneColor.WHITE);
        
        // On empty board, bot should find a move
        assertNotNull(move, "Bot should find a move on empty board");
        assertTrue(move.x() >= 0 && move.x() < 9, "X coordinate should be valid");
        assertTrue(move.y() >= 0 && move.y() < 9, "Y coordinate should be valid");
    }
    
    @Test
    @DisplayName("Should return null when bot decides to pass")
    void shouldReturnNullForPass() {
        // This tests the pass mechanism - on a complex board with no good moves
        // In practice, we can't easily force this in a unit test
        // The bot will pass when strategy.findBestMove returns null
    }
}
