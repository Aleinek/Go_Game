package com.gogame.controller;

import com.gogame.dto.response.GameHistoryResponse;
import com.gogame.dto.response.GameReplayResponse;
import com.gogame.repository.entity.GameDocument;
import com.gogame.repository.entity.MoveDocument;
import com.gogame.repository.entity.PlayerDocument;
import com.gogame.service.GameHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for game history and replay functionality.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Listing saved/finished games</li>
 *   <li>Retrieving game replay data</li>
 *   <li>Player game history</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/history")
public class GameHistoryController {

    private final GameHistoryService gameHistoryService;

    public GameHistoryController(GameHistoryService gameHistoryService) {
        this.gameHistoryService = gameHistoryService;
    }

    /**
     * Lists all finished games.
     * 
     * @return list of game summaries
     */
    @GetMapping("/games")
    public ResponseEntity<GameHistoryResponse> getFinishedGames() {
        List<GameDocument> games = gameHistoryService.getFinishedGames();
        
        List<GameHistoryResponse.GameSummary> summaries = games.stream()
            .map(this::toGameSummary)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new GameHistoryResponse(summaries, summaries.size()));
    }

    /**
     * Gets complete replay data for a specific game.
     * 
     * @param gameId the game UUID
     * @return complete game data for replay
     */
    @GetMapping("/games/{gameId}/replay")
    public ResponseEntity<GameReplayResponse> getGameReplay(@PathVariable UUID gameId) {
        return gameHistoryService.findGameById(gameId)
            .map(this::toReplayResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gets games for a specific player.
     * 
     * @param playerId the player UUID
     * @return list of games the player participated in
     */
    @GetMapping("/players/{playerId}/games")
    public ResponseEntity<GameHistoryResponse> getPlayerGames(@PathVariable UUID playerId) {
        List<GameDocument> games = gameHistoryService.getPlayerGames(playerId);
        
        List<GameHistoryResponse.GameSummary> summaries = games.stream()
            .map(this::toGameSummary)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new GameHistoryResponse(summaries, summaries.size()));
    }

    private GameHistoryResponse.GameSummary toGameSummary(GameDocument doc) {
        return new GameHistoryResponse.GameSummary(
            doc.getId(),
            doc.getBoardSize(),
            doc.getStatus(),
            toPlayerInfo(doc.getBlackPlayer()),
            toPlayerInfo(doc.getWhitePlayer()),
            doc.getWinner(),
            doc.getBlackScore(),
            doc.getWhiteScore(),
            doc.getMoves().size(),
            doc.getCreatedAt(),
            doc.getFinishedAt()
        );
    }

    private GameHistoryResponse.PlayerInfo toPlayerInfo(PlayerDocument player) {
        if (player == null) return null;
        return new GameHistoryResponse.PlayerInfo(
            player.getPlayerId(),
            player.getNickname(),
            player.getCapturedStones()
        );
    }

    private GameReplayResponse toReplayResponse(GameDocument doc) {
        List<GameReplayResponse.ReplayMove> moves = doc.getMoves().stream()
            .map(this::toReplayMove)
            .collect(Collectors.toList());

        return new GameReplayResponse(
            doc.getId(),
            doc.getBoardSize(),
            toReplayPlayerInfo(doc.getBlackPlayer()),
            toReplayPlayerInfo(doc.getWhitePlayer()),
            moves,
            doc.getStatus(),
            doc.getWinner(),
            doc.getBlackScore(),
            doc.getWhiteScore(),
            doc.getKomi(),
            doc.getCreatedAt(),
            doc.getFinishedAt()
        );
    }

    private GameReplayResponse.PlayerInfo toReplayPlayerInfo(PlayerDocument player) {
        if (player == null) return null;
        return new GameReplayResponse.PlayerInfo(
            player.getPlayerId(),
            player.getNickname(),
            player.getStoneColor(),
            player.getCapturedStones()
        );
    }

    private GameReplayResponse.ReplayMove toReplayMove(MoveDocument move) {
        return new GameReplayResponse.ReplayMove(
            move.getMoveNumber(),
            move.getPlayerColor(),
            move.getX(),
            move.getY(),
            move.isPass(),
            move.getCapturedStones(),
            move.getTimestamp()
        );
    }
}
