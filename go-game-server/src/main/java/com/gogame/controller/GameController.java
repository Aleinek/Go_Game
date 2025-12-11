package com.gogame.controller;

import com.gogame.dto.request.JoinGameRequest;
import com.gogame.dto.request.MakeMoveRequest;
import com.gogame.dto.response.BoardResponse;
import com.gogame.dto.response.GameResponse;
import com.gogame.dto.response.MoveResponse;
import com.gogame.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    private final Map<Integer, UUID> waitingQueue = new HashMap<>();
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody JoinGameRequest request) {
        
        int boardSize = request.boardSize();
        
        UUID waitingPlayerId = waitingQueue.get(boardSize);
        
        if (waitingPlayerId != null && !waitingPlayerId.equals(playerId)) {
            waitingQueue.remove(boardSize);
            
            GameResponse response = gameService.createGame(waitingPlayerId, playerId, boardSize);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            waitingQueue.put(boardSize, playerId);
            GameResponse response = new GameResponse(
                null, "WAITING", boardSize, null, null, null, 0, null, null, null,
                "Waiting for opponent..."
            );
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID id) {
        GameResponse response = gameService.getGame(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/moves")
    public ResponseEntity<MoveResponse> makeMove(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody MakeMoveRequest request) {
        
        MoveResponse response = gameService.makeMove(id, playerId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pass")
    public ResponseEntity<MoveResponse> pass(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        MoveResponse response = gameService.pass(id, playerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/resign")
    public ResponseEntity<GameResponse> resign(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        GameResponse response = gameService.resign(id, playerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/board")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable UUID id) {
        BoardResponse response = gameService.getBoard(id);
        return ResponseEntity.ok(response);
    }
}
