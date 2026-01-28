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

/**
 * REST controller handling game operations for Go.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Joining/creating games via matchmaking queue</li>
 *   <li>Making moves and passing</li>
 *   <li>Resigning from games</li>
 *   <li>Fetching game and board state</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
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

    /**
     * Creates a new game against a bot opponent.
     * <p>
     * Immediately starts a game without waiting for matchmaking.
     * The human player always plays as BLACK (moves first).
     * </p>
     * 
     * @param playerId the UUID of the human player from X-Player-Id header
     * @param request contains the desired board size (9, 13, or 19)
     * @return CREATED (201) with game details
     */
    @PostMapping("/join-with-bot")
    public ResponseEntity<GameResponse> joinGameWithBot(
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody JoinGameRequest request) {
        
        gameService.validatePlayerExists(playerId);
        
        GameResponse response = gameService.createGameWithBot(playerId, request.boardSize());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Joins a game or enters the matchmaking queue.
     * <p>
     * If another player is waiting for the same board size, creates a new game.
     * Otherwise, adds the player to the waiting queue.
     * </p>
     * 
     * @param playerId the UUID of the player from X-Player-Id header
     * @param request contains the desired board size (9, 13, or 19)
     * @return CREATED (201) with game details, or ACCEPTED (202) if waiting
     */
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

    /**
     * Checks the status of a waiting game request.
     * <p>
     * Used for polling until an opponent is found. Returns MATCHED with gameId
     * when a game has started, or WAITING if still in queue.
     * </p>
     * 
     * @param waitingGameId the UUID of the waiting request
     * @return status object with MATCHED/WAITING and optional gameId
     */
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

    /**
     * Retrieves the current state of a game.
     * 
     * @param id the game UUID
     * @return game details including status, players, and turn information
     * @throws GameNotFoundException if game doesn't exist
     */
    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getGame(@PathVariable UUID id) {
        GameResponse response = gameService.getGame(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Places a stone on the board.
     * 
     * @param id the game UUID
     * @param playerId the player making the move
     * @param request contains x and y coordinates
     * @return move result with captured stones and new board state
     * @throws InvalidMoveException if move is illegal (occupied, ko, suicide, etc.)
     */
    @PostMapping("/{id}/move")
    public ResponseEntity<MoveResponse> makeMove(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId,
            @RequestBody MakeMoveRequest request) {
        
        MoveResponse response = gameService.makeMove(id, playerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all moves made in the game.
     * 
     * @param id the game UUID
     * @return list of all moves with coordinates, colors, and timestamps
     */
    @GetMapping("/{id}/moves")
    public ResponseEntity<MovesListResponse> getMoves(@PathVariable UUID id) {
        MovesListResponse response = gameService.getMoves(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Passes the turn without placing a stone.
     * <p>
     * Two consecutive passes trigger the scoring negotiation phase.
     * </p>
     * 
     * @param id the game UUID
     * @param playerId the player passing
     * @return move response indicating pass was recorded
     */
    @PostMapping("/{id}/pass")
    public ResponseEntity<MoveResponse> pass(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        MoveResponse response = gameService.pass(id, playerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Resigns from the game, forfeiting to the opponent.
     * 
     * @param id the game UUID
     * @param playerId the player resigning
     * @return updated game state with RESIGNED status
     */
    @PostMapping("/{id}/resign")
    public ResponseEntity<GameResponse> resign(
            @PathVariable UUID id,
            @RequestHeader("X-Player-Id") UUID playerId) {
        
        GameResponse response = gameService.resign(id, playerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the current board state.
     * 
     * @param id the game UUID
     * @return board with all stones, captured counts, and territory
     */
    @GetMapping("/{id}/board")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable UUID id) {
        BoardResponse response = gameService.getBoard(id);
        return ResponseEntity.ok(response);
    }
}