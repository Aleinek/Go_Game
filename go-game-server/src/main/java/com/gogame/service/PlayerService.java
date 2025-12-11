package com.gogame.service;

import com.gogame.domain.exception.PlayerNotFoundException;
import com.gogame.domain.model.Player;
import com.gogame.dto.request.CreatePlayerRequest;
import com.gogame.dto.response.PlayerResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerService {
    
    private final Map<UUID, Player> players = new ConcurrentHashMap<>();
    private final Map<String, UUID> nicknameToId = new ConcurrentHashMap<>();
    private final Map<UUID, String> tokens = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> createdAt = new ConcurrentHashMap<>();
    
    public PlayerResponse createPlayer(CreatePlayerRequest request) {
        if (nicknameToId.containsKey(request.nickname())) {
            throw new IllegalArgumentException("Nickname '" + request.nickname() + "' is already taken");
        }
        
        UUID playerId = UUID.randomUUID();
        String token = generateToken(playerId);
        Instant now = Instant.now();
        
        Player player = new Player(playerId, request.nickname(), null);
        
        players.put(playerId, player);
        nicknameToId.put(request.nickname(), playerId);
        tokens.put(playerId, token);
        createdAt.put(playerId, now);
        
        return new PlayerResponse(playerId, request.nickname(), token, now);
    }
    
    public Player getPlayer(UUID playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new PlayerNotFoundException(playerId);
        }
        return player;
    }
    
    public PlayerResponse getPlayerResponse(UUID playerId) {
        Player player = getPlayer(playerId);
        return new PlayerResponse(
            playerId,
            player.getNickname(),
            tokens.get(playerId),
            createdAt.get(playerId)
        );
    }
    
    public boolean validateToken(UUID playerId, String token) {
        String storedToken = tokens.get(playerId);
        return storedToken != null && storedToken.equals(token);
    }
    
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players.values());
    }
    
    private String generateToken(UUID playerId) {
        return "token_" + playerId.toString() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    public void resetCapturedStones(UUID playerId) {
        getPlayer(playerId);
    }
}
