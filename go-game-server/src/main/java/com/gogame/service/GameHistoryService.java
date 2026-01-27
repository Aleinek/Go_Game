package com.gogame.service;

import com.gogame.domain.model.Game;
import com.gogame.domain.model.Move;
import com.gogame.domain.model.Player;
import com.gogame.domain.model.Position;
import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;
import com.gogame.repository.GameRepository;
import com.gogame.repository.entity.GameDocument;
import com.gogame.repository.entity.MoveDocument;
import com.gogame.repository.entity.PlayerDocument;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for persisting and retrieving game history from MongoDB.
 * <p>
 * Handles:
 * <ul>
 *   <li>Saving game state after each move</li>
 *   <li>Retrieving saved games for replay</li>
 *   <li>Converting between domain models and documents</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Service
public class GameHistoryService {

    private final GameRepository gameRepository;

    public GameHistoryService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Saves or updates the game state in MongoDB.
     * Called after each move, pass, or game status change.
     * 
     * @param game the current game state
     */
    public void saveGame(Game game) {
        Optional<GameDocument> existingDoc = gameRepository.findById(game.id);
        
        GameDocument doc;
        if (existingDoc.isPresent()) {
            doc = existingDoc.get();
        } else {
            doc = new GameDocument();
            doc.setId(game.id);
            doc.setCreatedAt(Instant.now());
        }
        
        updateDocumentFromGame(doc, game);
        gameRepository.save(doc);
    }

    /**
     * Retrieves a saved game by ID.
     * 
     * @param gameId the game UUID
     * @return the saved game document, or empty if not found
     */
    public Optional<GameDocument> findGameById(UUID gameId) {
        return gameRepository.findById(gameId);
    }

    /**
     * Retrieves all finished games (for history display).
     * 
     * @return list of finished games ordered by completion time
     */
    public List<GameDocument> getFinishedGames() {
        return gameRepository.findByStatusInOrderByFinishedAtDesc(
            List.of(GameStatus.FINISHED.name(), GameStatus.RESIGNED.name())
        );
    }

    /**
     * Retrieves games where a specific player participated.
     * 
     * @param playerId the player's UUID
     * @return list of games
     */
    public List<GameDocument> getPlayerGames(UUID playerId) {
        return gameRepository.findByPlayerId(playerId);
    }

    /**
     * Retrieves the move history for replay purposes.
     * Returns moves in order with complete position data.
     * 
     * @param gameId the game UUID
     * @return list of moves for replay
     */
    public List<MoveDocument> getMovesForReplay(UUID gameId) {
        return gameRepository.findById(gameId)
            .map(GameDocument::getMoves)
            .orElse(new ArrayList<>());
    }

    /**
     * Updates the document with current game state.
     */
    private void updateDocumentFromGame(GameDocument doc, Game game) {
        doc.setBoardSize(game.board.getSize());
        doc.setStatus(game.status.name());
        doc.setCurrentTurn(game.currentTurn.name());
        doc.setConsecutivePasses(game.consecutivePasses);
        doc.setKomi(game.komi);
        doc.setUpdatedAt(Instant.now());
        
        // Update players
        doc.setBlackPlayer(toPlayerDocument(game.blackPlayer));
        doc.setWhitePlayer(toPlayerDocument(game.whitePlayer));
        
        // Update moves
        doc.setMoves(game.moves.stream()
            .map(this::toMoveDocument)
            .collect(Collectors.toList()));
        
        // Handle finished games
        if (game.status == GameStatus.FINISHED || game.status == GameStatus.RESIGNED) {
            doc.setFinishedAt(Instant.now());
            
            if (game.finalScore != null) {
                doc.setWinner(game.finalScore.getWinner());
                doc.setBlackScore(game.finalScore.getBlackTotal());
                doc.setWhiteScore(game.finalScore.getWhiteTotal());
            } else if (game.status == GameStatus.RESIGNED) {
                Player winner = game.getWinner();
                if (winner != null) {
                    doc.setWinner(winner.getStoneColor().name());
                }
            }
        }
    }

    private PlayerDocument toPlayerDocument(Player player) {
        return new PlayerDocument(
            player.getId(),
            player.getNickname(),
            player.getStoneColor().name(),
            player.getCapturedStones()
        );
    }

    private MoveDocument toMoveDocument(Move move) {
        Position pos = move.getPosition();
        return new MoveDocument(
            move.id,
            move.getMoveNumber(),
            move.getPlayer().getId(),
            move.getPlayer().getStoneColor().name(),
            pos != null ? pos.getX() : null,
            pos != null ? pos.getY() : null,
            move.isPass(),
            move.getCapturedStones(),
            Instant.now()
        );
    }
}
