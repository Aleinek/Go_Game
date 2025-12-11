package com.gogame.controller;

import com.gogame.dto.request.CreatePlayerRequest;
import com.gogame.dto.response.PlayerResponse;
import com.gogame.service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }
    
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@RequestBody CreatePlayerRequest request) {
        PlayerResponse response = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable UUID id) {
        PlayerResponse response = playerService.getPlayerResponse(id);
        return ResponseEntity.ok(response);
    }
}
