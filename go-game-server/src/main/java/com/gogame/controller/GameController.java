package com.gogame.controller;

import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final Map<String, Game> games = new HashMap<>();

    @PostMapping("/create")
    public ResponseEntity<GameResponse> createGame(
            @RequestBody CreateGameRequest request) {
        
        Player blackPlayer = new Player(UUID.randomUUID(), request.blackPlayerName(), StoneColor.BLACK);
        Player whitePlayer = new Player(UUID.randomUUID(), request.whitePlayerName(), StoneColor.WHITE);
        
        Board board = new Board(request.boardSize(), blackPlayer, whitePlayer);
        Game game = new Game(blackPlayer, whitePlayer, request.boardSize(), null);
        String gameId = game.id.toString();
        games.put(gameId, game);
        
        return ResponseEntity.ok(new GameResponse(
            gameId,
            new PlayerDTO(blackPlayer.getId().toString(), blackPlayer.getNickname(), blackPlayer.getCapturedStones()),
            new PlayerDTO(whitePlayer.getId().toString(), whitePlayer.getNickname(), whitePlayer.getCapturedStones()),
            game.currentTurn.toString(),
            game.status.toString(),
            game.board.getSize(),
            game.moves.size(),
            game.consecutivePasses,
            "Game created successfully"
        ));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameResponse> getGame(@PathVariable String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(createGameResponse(gameId, game, "Game state retrieved"));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<?> makeMove(
            @PathVariable String gameId,
            @RequestBody MakeMoveRequest request) {
        
        Game game = games.get(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Position position = new Position(request.row(), request.col());
            MoveResult result = game.makeMove(position);
            
            if (result.getSuccess()) {
                return ResponseEntity.ok(createMoveResponse(gameId, game, result, "Move made successfully"));
            } else {
                return ResponseEntity.badRequest().body(new GameErrorResponse(
                    "INVALID_MOVE",
                    "Move failed"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GameErrorResponse(
                "ERROR",
                e.getMessage()
            ));
        }
    }

    @PostMapping("/{gameId}/pass")
    public ResponseEntity<?> pass(@PathVariable String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        MoveResult result = game.pass();
        return ResponseEntity.ok(createMoveResponse(gameId, game, result, "Player passed"));
    }

    @PostMapping("/{gameId}/resign")
    public ResponseEntity<GameResponse> resign(
            @PathVariable String gameId,
            @RequestBody ResignRequest request) {
        
        Game game = games.get(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        Player player = request.playerId().equals(game.blackPlayer.getId().toString()) 
            ? game.blackPlayer 
            : game.whitePlayer;
        
        game.resign(player);
        
        return ResponseEntity.ok(createGameResponse(gameId, game, "Player resigned"));
    }

    @GetMapping("/{gameId}/moves")
    public ResponseEntity<MovesResponse> getMoves(@PathVariable String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            return ResponseEntity.notFound().build();
        }

        List<MoveDTO> moveDTOs = new ArrayList<>();
        for (Move move : game.moves) {
            moveDTOs.add(new MoveDTO(
                move.id.toString(),
                move.player.getNickname(),
                move.isPass ? null : move.position.getX(),
                move.isPass ? null : move.position.getY(),
                move.moveNumber,
                move.isPass,
                move.capturedStones,
                move.timestamp.toString()
            ));
        }

        return ResponseEntity.ok(new MovesResponse(
            gameId,
            moveDTOs,
            "Moves retrieved successfully"
        ));
    }

    @GetMapping("/list")
    public ResponseEntity<List<GameSummaryDTO>> listGames() {
        List<GameSummaryDTO> summaries = new ArrayList<>();
        for (Map.Entry<String, Game> entry : games.entrySet()) {
            Game game = entry.getValue();
            summaries.add(new GameSummaryDTO(
                entry.getKey(),
                game.blackPlayer.getNickname(),
                game.whitePlayer.getNickname(),
                game.status.toString(),
                game.moves.size()
            ));
        }
        return ResponseEntity.ok(summaries);
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<String> deleteGame(@PathVariable String gameId) {
        Game removed = games.remove(gameId);
        if (removed == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Game deleted successfully");
    }

    private GameResponse createGameResponse(String gameId, Game game, String message) {
        return new GameResponse(
            gameId,
            new PlayerDTO(game.blackPlayer.getId().toString(), game.blackPlayer.getNickname(), game.blackPlayer.getCapturedStones()),
            new PlayerDTO(game.whitePlayer.getId().toString(), game.whitePlayer.getNickname(), game.whitePlayer.getCapturedStones()),
            game.currentTurn.toString(),
            game.status.toString(),
            game.board.getSize(),
            game.moves.size(),
            game.consecutivePasses,
            message
        );
    }

    private MoveResponse createMoveResponse(String gameId, Game game, MoveResult result, String message) {
        int capturedCount = result.getCapturedStones() != null ? result.getCapturedStones().size() : 0;
        return new MoveResponse(
            gameId,
            result.getSuccess(),
            game.currentTurn.toString(),
            game.status.toString(),
            game.moves.size(),
            capturedCount,
            game.isGameOver(),
            game.getWinner() != null ? game.getWinner().getNickname() : null,
            message
        );
    }
}

record CreateGameRequest(String blackPlayerName, String whitePlayerName, int boardSize) {}
record MakeMoveRequest(int row, int col) {}
record ResignRequest(String playerId) {}

record PlayerDTO(String id, String nickname, int capturedStones) {}

record GameResponse(
    String gameId,
    PlayerDTO blackPlayer,
    PlayerDTO whitePlayer,
    String currentTurn,
    String status,
    int boardSize,
    int moveCount,
    int consecutivePasses,
    String message
) {}

record MoveResponse(
    String gameId,
    boolean success,
    String currentTurn,
    String status,
    int moveCount,
    int capturedStones,
    boolean gameOver,
    String winner,
    String message
) {}

record MoveDTO(
    String id,
    String playerName,
    Integer row,
    Integer col,
    int moveNumber,
    boolean isPass,
    int capturedStones,
    String timestamp
) {}

record MovesResponse(
    String gameId,
    List<MoveDTO> moves,
    String message
) {}

record GameSummaryDTO(
    String gameId,
    String blackPlayer,
    String whitePlayer,
    String status,
    int moveCount
) {}

record GameErrorResponse(String errorCode, String message) {}
