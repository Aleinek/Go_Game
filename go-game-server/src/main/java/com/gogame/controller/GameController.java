package com.gogame.controller;

import com.gogame.dto.request.JoinGameRequest;
import com.gogame.dto.request.MakeMoveRequest;
import com.gogame.dto.response.BoardResponse;
import com.gogame.dto.response.GameResponse;
import com.gogame.dto.response.MoveResponse;
import com.gogame.dto.response.MovesListResponse;
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
    private final Map<UUID, WaitingGame> waitingGames = new HashMap<>();
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    private record WaitingGame(UUID playerId, int boardSize, UUID actualGameId) {}

    @PostMapping("/join")
    public ResponseEntity<GameResponse> joinGame(
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody JoinGameRequest request) {
        
        // Validate player exists before adding to queue
        gameService.validatePlayerExists(playerId);
        
        int boardSize = request.boardSize();
        
        UUID waitingPlayerId = waitingQueue.get(boardSize);
        
        if (waitingPlayerId != null && !waitingPlayerId.equals(playerId)) {
            waitingQueue.remove(boardSize);
            
            GameResponse response = gameService.createGame(waitingPlayerId, playerId, boardSize);
            
            // podmianka waitingGame na GameId żeby można było sprawdzić status oczekiwania
            for (Map.Entry<UUID, WaitingGame> entry : waitingGames.entrySet()) {
                if (entry.getValue().playerId().equals(waitingPlayerId)) {
                    UUID waitingGameId = entry.getKey();
                    waitingGames.put(waitingGameId, 
                        new WaitingGame(waitingPlayerId, boardSize, response.id()));
                    break;
                }
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            UUID waitingGameId = UUID.randomUUID();
            waitingQueue.put(boardSize, playerId);
            waitingGames.put(waitingGameId, new WaitingGame(playerId, boardSize, null));
            
            GameResponse response = new GameResponse(
                waitingGameId, "WAITING", boardSize, null, null, null, 0, null, null, null,
                "Waiting for opponent..."
            );
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
    }

    @GetMapping("/waiting/{waitingGameId}")
    public ResponseEntity<Map<String, Object>> checkWaitingStatus(@PathVariable UUID waitingGameId) {
        WaitingGame waitingGame = waitingGames.get(waitingGameId);
        
        if (waitingGame == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        if (waitingGame.actualGameId() != null) {
            // response po starcie gry
            response.put("status", "MATCHED");
            response.put("gameId", waitingGame.actualGameId());
            waitingGames.remove(waitingGameId);
        } else {
            // response jak nie znalazło gracza
            response.put("status", "WAITING");
            response.put("message", "Waiting for opponent...");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID id) {
        GameResponse response = gameService.getGame(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/move")
    public ResponseEntity<MoveResponse> makeMove(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody MakeMoveRequest request) {
        
        MoveResponse response = gameService.makeMove(id, playerId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/moves")
    // gets all moves made so far in the game by both players on {id}
    public ResponseEntity<MovesListResponse> getMoves(@PathVariable UUID id) {
        MovesListResponse response = gameService.getMoves(id);
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