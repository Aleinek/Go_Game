package com.gogame.bot;

import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.Game;
import com.gogame.domain.model.Player;
import com.gogame.dto.request.MakeMoveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Scheduler component for executing bot moves with realistic delays.
 * <p>
 * Uses async execution to prevent blocking the main game thread.
 * Adds random delay to simulate "thinking" time.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Component
@EnableAsync
public class BotMoveScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(BotMoveScheduler.class);
    
    private static final int MIN_DELAY_MS = 500;
    private static final int MAX_DELAY_MS = 1500;
    
    private final BotService botService;
    private final Random random = new Random();
    
    public BotMoveScheduler(BotService botService) {
        this.botService = botService;
    }
    
    /**
     * Schedules a bot move to be executed asynchronously after a random delay.
     * 
     * @param game the current game state
     * @param botPlayer the bot player who should move
     * @param moveCallback callback function to execute the move (gameId, moveRequest)
     * @param passCallback callback function to execute a pass (gameId, playerId)
     */
    @Async
    public void scheduleBotMove(
            Game game,
            Player botPlayer,
            BiConsumer<UUID, MakeMoveRequest> moveCallback,
            BiConsumer<UUID, UUID> passCallback) {
        
        try {
            // Simulate thinking time
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            log.debug("Bot scheduling move in {} ms for game {}", delay, game.id);
            Thread.sleep(delay);
            
            // Verify game is still in progress
            if (game.status != GameStatus.IN_PROGRESS) {
                log.debug("Game {} no longer in progress, bot move cancelled", game.id);
                return;
            }
            
            // Verify it's still the bot's turn
            if (!game.getCurrentPlayer().getId().equals(botPlayer.getId())) {
                log.debug("No longer bot's turn in game {}, move cancelled", game.id);
                return;
            }
            
            StoneColor botColor = botPlayer.getStoneColor();
            MakeMoveRequest moveRequest = botService.calculateMove(game, botColor);
            
            if (moveRequest != null) {
                // Execute the move
                moveCallback.accept(game.id, moveRequest);
                log.info("Bot executed move at ({}, {}) in game {}", 
                    moveRequest.x(), moveRequest.y(), game.id);
            } else {
                // Bot passes
                passCallback.accept(game.id, botPlayer.getId());
                log.info("Bot passed in game {}", game.id);
            }
            
        } catch (InterruptedException e) {
            log.warn("Bot move interrupted for game {}", game.id);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error executing bot move for game {}: {}", game.id, e.getMessage(), e);
        }
    }
    
    /**
     * Checks if a player is a bot based on nickname convention.
     */
    public boolean isBot(Player player) {
        return botService.isBotPlayer(player.getNickname());
    }
    
    /**
     * Schedules automatic score acceptance for a bot player during negotiation.
     * 
     * @param game the current game state
     * @param botPlayer the bot player who should accept
     * @param acceptCallback callback function to execute the acceptance (gameId, playerId)
     */
    @Async
    public void scheduleBotNegotiationAccept(
            Game game,
            Player botPlayer,
            BiConsumer<UUID, UUID> acceptCallback) {
        
        try {
            // Short delay before accepting (simulate reviewing the board)
            int delay = MIN_DELAY_MS + random.nextInt(MAX_DELAY_MS - MIN_DELAY_MS);
            log.debug("Bot scheduling negotiation accept in {} ms for game {}", delay, game.id);
            Thread.sleep(delay);
            
            // Verify game is still in negotiation
            if (game.status != GameStatus.NEGOTIATING) {
                log.debug("Game {} no longer in negotiation, bot accept cancelled", game.id);
                return;
            }
            
            // Execute the acceptance
            acceptCallback.accept(game.id, botPlayer.getId());
            log.info("Bot {} accepted score in game {}", botPlayer.getNickname(), game.id);
            
        } catch (InterruptedException e) {
            log.warn("Bot negotiation accept interrupted for game {}", game.id);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Error executing bot negotiation accept for game {}: {}", game.id, e.getMessage(), e);
        }
    }
}
