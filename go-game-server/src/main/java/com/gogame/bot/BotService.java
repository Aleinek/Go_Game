package com.gogame.bot;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.Board;
import com.gogame.domain.model.Game;
import com.gogame.domain.model.Position;
import com.gogame.dto.request.MakeMoveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Service for managing bot players in Go games.
 * <p>
 * Provides functionality for:
 * <ul>
 *   <li>Calculating and executing bot moves</li>
 *   <li>Coordinating with GameService for move execution</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Service
public class BotService {
    
    private static final Logger log = LoggerFactory.getLogger(BotService.class);
    
    private final BotStrategy strategy;
    
    public BotService() {
        this.strategy = new BotStrategy();
    }
    
    /**
     * Calculates the best move for a bot player.
     * 
     * @param game the current game state
     * @param botColor the bot's stone color
     * @return MakeMoveRequest with the chosen position, or null if bot should pass
     */
    public MakeMoveRequest calculateMove(Game game, StoneColor botColor) {
        Board board = game.board;
        int moveCount = game.moves.size();
        
        log.info("Bot ({}) calculating move for game {}, move #{}", 
            botColor, game.id, moveCount + 1);
        
        Position bestMove = strategy.findBestMove(board, botColor, moveCount);
        
        if (bestMove == null) {
            log.info("Bot ({}) decided to pass in game {}", botColor, game.id);
            return null; // Signal to pass
        }
        
        log.info("Bot ({}) chose move at ({}, {}) in game {}", 
            botColor, bestMove.getX(), bestMove.getY(), game.id);
        
        return new MakeMoveRequest(bestMove.getX(), bestMove.getY());
    }
    
    /**
     * Determines if a player ID represents a bot.
     * Uses naming convention: bot nicknames start with "Bot-"
     * 
     * @param nickname the player's nickname
     * @return true if this is a bot player
     */
    public boolean isBotPlayer(String nickname) {
        return nickname != null && nickname.startsWith("Bot-");
    }
}
