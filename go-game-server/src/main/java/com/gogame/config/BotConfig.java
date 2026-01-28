package com.gogame.config;

import com.gogame.bot.BotMoveScheduler;
import com.gogame.service.GameService;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * Configuration class for setting up bot-related dependencies.
 * <p>
 * Uses setter injection to avoid circular dependency between
 * GameService and BotMoveScheduler.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Configuration
public class BotConfig {
    
    private final GameService gameService;
    private final BotMoveScheduler botMoveScheduler;
    
    public BotConfig(GameService gameService, BotMoveScheduler botMoveScheduler) {
        this.gameService = gameService;
        this.botMoveScheduler = botMoveScheduler;
    }
    
    @PostConstruct
    public void configureBotScheduler() {
        gameService.setBotMoveScheduler(botMoveScheduler);
    }
}
