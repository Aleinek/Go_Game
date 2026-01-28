package com.gogame.service;

import com.gogame.domain.exception.PlayerNotFoundException;
import com.gogame.domain.model.Player;
import com.gogame.dto.request.CreatePlayerRequest;
import com.gogame.dto.response.PlayerResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for player management operations.
 * <p>
 * Handles player registration, retrieval, and token validation.
 * Players are stored in memory with unique nicknames.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Service
public class PlayerService {
    
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private final Map<String, UUID> nicknameToId = new ConcurrentHashMap<>();
    private final Map<UUID, String> tokens = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> createdAt = new ConcurrentHashMap<>();
    
    /**
     * Creates a new player with the given nickname.
     * 
     * @param request contains the desired nickname
     * @return PlayerResponse with ID, nickname, token, and creation time
     * @throws IllegalArgumentException if nickname is already taken
     */
    public PlayerResponse createPlayer(CreatePlayerRequest request) {
        if (nicknameToId.containsKey(request.nickname())) {
            throw new IllegalArgumentException("Nickname '" + request.nickname() + "' is already taken");
        }
        
        UUID playerId = UUID.randomUUID();
        String token = generateToken(playerId);
        Instant now = Instant.now();
        
        Player player = new Player(playerId, request.nickname(), null, false);
        
        players.put(playerId, player);
        nicknameToId.put(request.nickname(), playerId);
        tokens.put(playerId, token);
        createdAt.put(playerId, now);
        
        return new PlayerResponse(playerId, request.nickname(), token, now);
    }
    
    /**
     * Retrieves a player by ID.
     * 
     * @param playerId the player's UUID
     * @return the Player entity
     * @throws PlayerNotFoundException if player doesn't exist
     */
    public Player getPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new PlayerNotFoundException(playerId);
        }
        return player;
    }
    
    /**
     * Retrieves player information as a response DTO.
     * 
     * @param playerId the player's UUID
     * @return PlayerResponse with player details
     */
    public PlayerResponse getPlayerResponse(UUID playerId) {
        Player player = getPlayer(playerId);
        return new PlayerResponse(
            playerId,
            player.getNickname(),
            tokens.get(playerId),
            createdAt.get(playerId)
        );
    }
    
    /**
     * Validates a player's authentication token.
     * 
     * @param playerId the player's UUID
     * @param token the token to validate
     * @return true if the token matches
     */
    public boolean validateToken(UUID playerId, String token) {
        String storedToken = tokens.get(playerId);
        return storedToken != null && storedToken.equals(token);
    }
    
    /**
     * Retrieves all registered players.
     * 
     * @return list of all Player entities
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }
    
    private String generateToken(UUID playerId) {
        return "token_" + playerId.toString() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Resets a player's captured stone count.
     * <p>
     * Used when starting a new game.
     * </p>
     * 
     * @param playerId the player's UUID
     */
    public void resetCapturedStones(UUID playerId) {
        getPlayer(playerId);
    }
    
    /**
     * Creates a bot player with an auto-generated nickname.
     * 
     * @return PlayerResponse for the created bot
     */
    public PlayerResponse createBotPlayer() {
        String botNickname = "Bot-" + UUID.randomUUID().toString().substring(0, 6);
        UUID botId = UUID.randomUUID();
        String token = generateToken(botId);
        Instant now = Instant.now();
        
        Player botPlayer = new Player(botId, botNickname, null, true);
        
        players.put(botId, botPlayer);
        nicknameToId.put(botNickname, botId);
        tokens.put(botId, token);
        createdAt.put(botId, now);
        
        return new PlayerResponse(botId, botNickname, token, now);
    }
    
    /**
     * Checks if a player is a bot.
     * 
     * @param playerId the player's UUID
     * @return true if the player is a bot
     */
    public boolean isBot(UUID playerId) {
        Player player = players.get(playerId);
        return player != null && player.isBot();
    }
}
