package com.gogame.service;

import com.gogame.domain.enums.DeadStoneStatus;
import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;
import com.gogame.domain.exception.GameNotFoundException;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.exception.InvalidNegotiationException;
import com.gogame.domain.model.*;
import com.gogame.dto.request.MakeMoveRequest;
import com.gogame.dto.response.BoardResponse;
import com.gogame.dto.response.GameResponse;
import com.gogame.dto.response.MoveResponse;
import com.gogame.dto.response.MovesListResponse;
import com.gogame.websocket.GameEventPayloads;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameService {
    
    private final Map<UUID, Game> games = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> gameCreatedAt = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> gameUpdatedAt = new ConcurrentHashMap<>();
    
    private final PlayerService playerService;
    private final BoardService boardService;
    private final GameNotificationService notificationService;
    private final NegotiationService negotiationService;
    
    public GameService(PlayerService playerService, BoardService boardService,
                      GameNotificationService notificationService,
                      NegotiationService negotiationService) {
        this.playerService = playerService;
        this.boardService = boardService;
        this.notificationService = notificationService;
        this.negotiationService = negotiationService;
    }
    
    public GameResponse createGame(UUID blackPlayerId, UUID whitePlayerId, int boardSize) {
        // Validate that both players exist (throws PlayerNotFoundException if not)
        Player existingBlackPlayer = playerService.getPlayer(blackPlayerId);
        Player existingWhitePlayer = playerService.getPlayer(whitePlayerId);
        
        // Create game-specific player instances with stone colors
        Player blackPlayer = new Player(
            blackPlayerId, 
            existingBlackPlayer.getNickname(), 
            StoneColor.BLACK
        );
        Player whitePlayer = new Player(
            whitePlayerId, 
            existingWhitePlayer.getNickname(), 
            StoneColor.WHITE
        );
        
        Board board = new Board(boardSize, blackPlayer, whitePlayer);
        
        Game game = new Game(blackPlayer, whitePlayer, board);
        
        Instant now = Instant.now();
        games.put(game.id, game);
        gameCreatedAt.put(game.id, now);
        gameUpdatedAt.put(game.id, now);
        notificationService.notifyGameStarted(
            blackPlayerId, game.id, "BLACK", whitePlayer.getNickname(), boardSize
        );
        notificationService.notifyGameStarted(
            whitePlayerId, game.id, "WHITE", blackPlayer.getNickname(), boardSize
        );
        
        return buildGameResponse(game, "Game created successfully");
    }
    
    public void validatePlayerExists(UUID playerId) {
        playerService.getPlayer(playerId);
    }
    
    public GameResponse getGame(UUID gameId) {
        Game game = findGame(gameId);
        return buildGameResponse(game, "Game state retrieved");
    }
    
    public MoveResponse makeMove(UUID gameId, UUID playerId, MakeMoveRequest request) {
        Game game = findGame(gameId);
        if (game.status != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException(
                InvalidMoveException.ErrorCode.GAME_NOT_IN_PROGRESS,
                "Game is not in progress. Status: " + game.status
            );
        }
        
        Player currentPlayer = game.getCurrentPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            throw new InvalidMoveException(
                InvalidMoveException.ErrorCode.NOT_YOUR_TURN,
                "It's not your turn"
            );
        }
        
        Position position = new Position(request.x(), request.y());
        StoneColor currentColor = game.currentTurn;
        
        List<Position> capturedPositions = boardService.applyMove(
            game.board, 
            position, 
            currentColor,
            game.blackPlayer,
            game.whitePlayer
        );

        Move move = new Move(currentPlayer, position, game.moves.size() + 1);
        game.moves.add(move);
        game.consecutivePasses = 0;

        game.switchTurn();
        gameUpdatedAt.put(gameId, Instant.now());

        UUID opponentId = currentPlayer.getId().equals(game.blackPlayer.getId()) 
            ? game.whitePlayer.getId() 
            : game.blackPlayer.getId();
        
        notificationService.notifyOpponentMoved(
            opponentId,
            move.getMoveNumber(),
            position.getX(),
            position.getY(),
            currentColor.toString(),
            capturedPositions.stream()
                .map(p -> new com.gogame.websocket.GameEventPayloads.PositionInfo(p.getX(), p.getY()))
                .collect(Collectors.toList()),
            game.currentTurn.toString()
        );

        return buildMoveResponse(game, move, capturedPositions, "Move made successfully");
    }

    public MovesListResponse getMoves(UUID gameId) {
        Game game = findGame(gameId);
        
        List<MovesListResponse.MoveInfo> moveInfos = game.moves.stream()
            .map(move -> {
                Position pos = move.getPosition();
                String color = move.getPlayer().getStoneColor().toString();
                return new MovesListResponse.MoveInfo(
                    move.getMoveNumber(),
                    pos != null ? pos.getX() : null,
                    pos != null ? pos.getY() : null,
                    color,
                    Instant.now()
                );
            })
            .collect(Collectors.toList());
        
        return new MovesListResponse(gameId, moveInfos, "Moves retrieved successfully");
    }
    
    public MoveResponse pass(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        if (game.status != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException(
                InvalidMoveException.ErrorCode.GAME_NOT_IN_PROGRESS,
                "Game is not in progress"
            );
        }
        
        Player currentPlayer = game.getCurrentPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            throw new InvalidMoveException(
                InvalidMoveException.ErrorCode.NOT_YOUR_TURN,
                "It's not your turn"
            );
        }
        
        Move move = Move.pass(currentPlayer, game.moves.size() + 1);
        game.moves.add(move);
        game.consecutivePasses++;
        
        String message;
        if (game.consecutivePasses >= 2) {
            // Start negotiation phase instead of ending the game
            game.startNegotiation();
            initializeNegotiationState(game);
            message = "Both players passed. Entering scoring negotiation phase.";
        } else {
            message = "Pass recorded. Opponent's turn.";
        }
        
        game.switchTurn();
        gameUpdatedAt.put(gameId, Instant.now());
        
        UUID opponentId = currentPlayer.getId().equals(game.blackPlayer.getId()) 
            ? game.whitePlayer.getId() 
            : game.blackPlayer.getId();
        
        notificationService.notifyOpponentPassed(
            opponentId,
            move.getMoveNumber(),
            game.consecutivePasses,
            game.currentTurn.toString()
        );
        
        // If negotiation started, send negotiation events to both players
        if (game.status == GameStatus.NEGOTIATING) {
            List<GameEventPayloads.ChainSuggestion> chainSuggestions = 
                buildChainSuggestions(game.negotiationState);
            
            notificationService.notifyNegotiationStarted(
                game.blackPlayer.getId(), chainSuggestions, game.komi
            );
            notificationService.notifyNegotiationStarted(
                game.whitePlayer.getId(), chainSuggestions, game.komi
            );
        }
        
        return buildMoveResponse(game, move, Collections.emptyList(), message);
    }
    
    /**
     * Initialize negotiation state with chain analysis and dead stone suggestions.
     */
    private void initializeNegotiationState(Game game) {
        List<ChainInfo> chainInfos = negotiationService.analyzeAndSuggestDeadChains(game.board);
        for (ChainInfo chainInfo : chainInfos) {
            game.negotiationState.addChainInfo(chainInfo);
        }
    }
    
    /**
     * Build chain suggestions payload for WebSocket notification.
     */
    private List<GameEventPayloads.ChainSuggestion> buildChainSuggestions(NegotiationState state) {
        return state.getAllChainInfos().values().stream()
            .map(ci -> new GameEventPayloads.ChainSuggestion(
                ci.getChainId(),
                ci.getPositions().stream()
                    .map(p -> new GameEventPayloads.PositionInfo(p.getX(), p.getY()))
                    .collect(Collectors.toList()),
                ci.getColor().toString(),
                ci.getLiberties(),
                ci.getStatus().toString()
            ))
            .collect(Collectors.toList());
    }
    
    public GameResponse resign(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        
        Player resigningPlayer = playerService.getPlayer(playerId);
        game.resign(resigningPlayer);
        
        gameUpdatedAt.put(gameId, Instant.now());
        
        Player winner = game.getWinner();
        notificationService.notifyGameEnded(
            game.blackPlayer.getId(),
            "RESIGNATION",
            winner.getNickname(),
            resigningPlayer.getNickname()
        );
        notificationService.notifyGameEnded(
            game.whitePlayer.getId(),
            "RESIGNATION",
            winner.getNickname(),
            resigningPlayer.getNickname()
        );
        
        String message = resigningPlayer.getNickname() + " resigned. " + 
                        winner.getNickname() + " wins!";
        
        return buildGameResponse(game, message);
    }
    
    // ==================== NEGOTIATION PHASE METHODS ====================
    
    /**
     * Toggle the dead/alive status of a chain during negotiation.
     * Validates that marking is logical before applying.
     */
    public void toggleChainStatus(UUID gameId, UUID playerId, int chainId) {
        Game game = findGame(gameId);
        validateNegotiationAccess(game, playerId);
        
        NegotiationState state = game.negotiationState;
        ChainInfo chainInfo = state.getChainInfo(chainId);
        
        if (chainInfo == null) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.CHAIN_NOT_FOUND, chainId
            );
        }
        
        // Determine new status before validation
        DeadStoneStatus newStatus = (chainInfo.getStatus() == DeadStoneStatus.ALIVE) 
            ? DeadStoneStatus.DEAD 
            : DeadStoneStatus.ALIVE;
        
        // Validate if marking as dead
        negotiationService.validateDeadMarking(game.board, chainInfo, newStatus);
        
        // Apply the toggle
        state.toggleChainStatus(chainId);
        gameUpdatedAt.put(gameId, Instant.now());
        
        // Determine who changed
        String changedBy = playerId.equals(game.blackPlayer.getId()) ? "BLACK" : "WHITE";
        
        // Notify both players
        notificationService.notifyChainStatusChanged(
            game.blackPlayer.getId(),
            chainId,
            chainInfo.getStatus().toString(),
            changedBy,
            state.isBlackAccepted(),
            state.isWhiteAccepted()
        );
        notificationService.notifyChainStatusChanged(
            game.whitePlayer.getId(),
            chainId,
            chainInfo.getStatus().toString(),
            changedBy,
            state.isBlackAccepted(),
            state.isWhiteAccepted()
        );
    }
    
    /**
     * Accept the current score. Game ends when both players accept.
     */
    public void acceptScore(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        validateNegotiationAccess(game, playerId);
        
        NegotiationState state = game.negotiationState;
        
        // Set acceptance for the player
        if (playerId.equals(game.blackPlayer.getId())) {
            state.setBlackAccepted(true);
        } else {
            state.setWhiteAccepted(true);
        }
        
        gameUpdatedAt.put(gameId, Instant.now());
        
        String acceptedBy = playerId.equals(game.blackPlayer.getId()) ? "BLACK" : "WHITE";
        
        // Check if both have accepted
        if (state.bothAccepted()) {
            // Calculate final score and end game
            ScoreResult score = negotiationService.calculateFinalScore(game);
            game.finishWithScore(score);
            
            GameEventPayloads.ScoreBreakdown scoreBreakdown = buildScoreBreakdown(score);
            
            // Notify game ended with score
            notificationService.notifyGameEndedWithScore(
                game.blackPlayer.getId(),
                "SCORING_COMPLETE",
                score.getWinner(),
                scoreBreakdown
            );
            notificationService.notifyGameEndedWithScore(
                game.whitePlayer.getId(),
                "SCORING_COMPLETE",
                score.getWinner(),
                scoreBreakdown
            );
        } else {
            // Notify about acceptance
            ScoreResult currentScore = negotiationService.calculateFinalScore(game);
            GameEventPayloads.ScoreBreakdown scoreBreakdown = buildScoreBreakdown(currentScore);
            
            notificationService.notifyScoreAccepted(
                game.blackPlayer.getId(),
                acceptedBy,
                state.isBlackAccepted(),
                state.isWhiteAccepted(),
                scoreBreakdown
            );
            notificationService.notifyScoreAccepted(
                game.whitePlayer.getId(),
                acceptedBy,
                state.isBlackAccepted(),
                state.isWhiteAccepted(),
                scoreBreakdown
            );
        }
    }
    
    /**
     * Resume playing from negotiation phase.
     * The requesting player gets the next turn.
     */
    public void resumePlay(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        validateNegotiationAccess(game, playerId);
        
        StoneColor requestingColor = playerId.equals(game.blackPlayer.getId()) 
            ? StoneColor.BLACK 
            : StoneColor.WHITE;
        
        String resumedByNickname = playerId.equals(game.blackPlayer.getId())
            ? game.blackPlayer.getNickname()
            : game.whitePlayer.getNickname();
        
        game.resumePlaying(requestingColor);
        gameUpdatedAt.put(gameId, Instant.now());
        
        // Notify both players
        notificationService.notifyGameResumed(
            game.blackPlayer.getId(),
            resumedByNickname,
            game.currentTurn.toString()
        );
        notificationService.notifyGameResumed(
            game.whitePlayer.getId(),
            resumedByNickname,
            game.currentTurn.toString()
        );
    }
    
    /**
     * Get current negotiation state including chains and acceptance status.
     */
    public NegotiationState getNegotiationState(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        validateNegotiationAccess(game, playerId);
        return game.negotiationState;
    }
    
    /**
     * Calculate and return current score (preview during negotiation).
     */
    public ScoreResult getScorePreview(UUID gameId, UUID playerId) {
        Game game = findGame(gameId);
        validateNegotiationAccess(game, playerId);
        return negotiationService.calculateFinalScore(game);
    }
    
    private void validateNegotiationAccess(Game game, UUID playerId) {
        if (game.status != GameStatus.NEGOTIATING) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.GAME_NOT_IN_NEGOTIATION
            );
        }
        
        if (!playerId.equals(game.blackPlayer.getId()) && 
            !playerId.equals(game.whitePlayer.getId())) {
            throw new InvalidNegotiationException(
                InvalidNegotiationException.ErrorCode.NOT_YOUR_GAME
            );
        }
    }
    
    private GameEventPayloads.ScoreBreakdown buildScoreBreakdown(ScoreResult score) {
        return new GameEventPayloads.ScoreBreakdown(
            score.getBlackTerritory(),
            score.getWhiteTerritory(),
            score.getBlackPrisoners(),
            score.getWhitePrisoners(),
            score.getBlackDeadStones(),
            score.getWhiteDeadStones(),
            score.getKomi(),
            score.getBlackTotal(),
            score.getWhiteTotal(),
            score.getWinner(),
            score.getScoreDifference()
        );
    }
    
    // ==================== END NEGOTIATION METHODS ====================
    
    public BoardResponse getBoard(UUID gameId) {
        Game game = findGame(gameId);
        return boardService.getBoardResponse(
            gameId,
            game.board,
            game.moves.size(),
            game.blackPlayer.getCapturedStones(),
            game.whitePlayer.getCapturedStones()
        );
    }
    
    public List<GameResponse> getAllGames() {
        return games.values().stream()
            .map(game -> buildGameResponse(game, null))
            .collect(Collectors.toList());
    }
    
    private Game findGame(UUID gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }
    
    private GameResponse buildGameResponse(Game game, String message) {
        Move lastMove = game.moves.isEmpty() ? null : game.moves.get(game.moves.size() - 1);
        
        GameResponse.MoveInfo lastMoveInfo = null;
        if (lastMove != null) {
            Position pos = lastMove.getPosition();
            lastMoveInfo = new GameResponse.MoveInfo(
                lastMove.getMoveNumber(),
                pos != null ? pos.getX() : null,
                pos != null ? pos.getY() : null,
                lastMove.getPlayer().getStoneColor() != null ? 
                    lastMove.getPlayer().getStoneColor().toString() : null,
                0,
                Instant.now()
            );
        }
        
        return new GameResponse(
            game.id,
            game.status.toString(),
            game.board.getSize(),
            game.currentTurn.toString(),
            new GameResponse.PlayerInfo(
                game.blackPlayer.getId(),
                game.blackPlayer.getNickname(),
                game.blackPlayer.getCapturedStones()
            ),
            new GameResponse.PlayerInfo(
                game.whitePlayer.getId(),
                game.whitePlayer.getNickname(),
                game.whitePlayer.getCapturedStones()
            ),
            game.moves.size(),
            lastMoveInfo,
            gameCreatedAt.get(game.id),
            gameUpdatedAt.get(game.id),
            message
        );
    }
    
    private MoveResponse buildMoveResponse(Game game, Move move, List<Position> capturedPositions, String message) {
        Position pos = move.getPosition();
        String color = move.getPlayer().getStoneColor().toString();
        
        MoveResponse.MoveInfo moveInfo = new MoveResponse.MoveInfo(
            move.getMoveNumber(),
            pos != null ? pos.getX() : -1,
            pos != null ? pos.getY() : -1,
            color,
            capturedPositions.size(),
            Instant.now()
        );
        
        List<MoveResponse.PositionInfo> capturedList = capturedPositions.stream()
            .map(p -> new MoveResponse.PositionInfo(p.getX(), p.getY()))
            .collect(Collectors.toList());
        
        List<MoveResponse.StoneInfo> stones = new ArrayList<>();
        for (int x = 0; x < game.board.getSize(); x++) {
            for (int y = 0; y < game.board.getSize(); y++) {
                Position position = new Position(x, y);
                Stone stone = game.board.getStoneAt(position);
                if (stone != null && stone.getColor() != StoneColor.EMPTY) {
                    stones.add(new MoveResponse.StoneInfo(x, y, stone.getColor().toString()));
                }
            }
        }
        
        MoveResponse.BoardInfo boardInfo = new MoveResponse.BoardInfo(game.board.getSize(), stones);
        
        return new MoveResponse(
            true,
            moveInfo,
            capturedList,
            game.currentTurn.toString(),
            boardInfo,
            message
        );
    }
}
