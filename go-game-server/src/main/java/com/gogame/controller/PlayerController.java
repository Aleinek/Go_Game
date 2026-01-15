package com.gogame.controller;

import com.gogame.dto.request.CreatePlayerRequest;
import com.gogame.dto.response.PlayerResponse;
import com.gogame.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for player management operations.
 * <p>
 * Handles player registration and retrieval. Players must be registered
 * before they can join games.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    
    /**
     * Constructs a PlayerController with required dependencies.
     * 
     * @param playerService service for player operations
     */
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    /**
     * Registers a new player with the given nickname.
     * 
     * @param request contains the desired nickname (3-20 alphanumeric characters)
     * @return CREATED (201) with player details and authentication token
     * @throws IllegalArgumentException if nickname is invalid or already taken
     */
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@RequestBody CreatePlayerRequest request) {
        PlayerResponse response = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieves player information by ID.
     * 
     * @param id the player UUID
     * @return player details including nickname and creation time
     * @throws PlayerNotFoundException if player doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable UUID id) {
        PlayerResponse response = playerService.getPlayerResponse(id);
        return ResponseEntity.ok(response);
    }
}
