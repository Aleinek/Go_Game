package com.gogame.service;

import com.gogame.websocket.GameEvent;
import com.gogame.websocket.GameEventPayloads;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for sending real-time WebSocket notifications to players.
 * <p>
 * Uses STOMP over WebSocket to push game events to subscribed clients.
 * Each player subscribes to their personal topic: {@code /topic/game/{playerId}}
 * </p>
 * <p>
 * Event types include:
 * <ul>
 *   <li>GAME_STARTED - Match found, game begins</li>
 *   <li>OPPONENT_MOVED - Opponent placed a stone</li>
 *   <li>OPPONENT_PASSED - Opponent passed their turn</li>
 *   <li>GAME_ENDED - Game finished (resignation, scoring complete)</li>
 *   <li>NEGOTIATION_* - Scoring phase events</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Service
public class GameNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public GameNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    /**
     * Notifies a player that a game has started.
     * 
     * @param playerId the player to notify
     * @param gameId the new game's ID
     * @param playerColor the player's assigned color (BLACK/WHITE)
     * @param opponentNickname the opponent's nickname
     * @param boardSize the board size (9, 13, or 19)
     */
    public void notifyGameStarted(UUID playerId, UUID gameId, String playerColor, 
                                   String opponentNickname, int boardSize) {
        GameEventPayloads.GameStartedPayload payload = new GameEventPayloads.GameStartedPayload(
            gameId,
            playerColor,
            new GameEventPayloads.OpponentInfo(opponentNickname),
            boardSize
        );
        
        GameEvent event = new GameEvent(GameEvent.GAME_STARTED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that their opponent has placed a stone.
     * 
     * @param playerId the player to notify
     * @param moveNumber the move sequence number
     * @param x the x coordinate of the move
     * @param y the y coordinate of the move
     * @param color the stone color placed
     * @param capturedPositions list of positions where stones were captured
     * @param currentTurn whose turn it is now
     */
    public void notifyOpponentMoved(UUID playerId, int moveNumber, int x, int y, 
                                    String color, List<GameEventPayloads.PositionInfo> capturedPositions,
                                    String currentTurn) {
        GameEventPayloads.MoveInfo moveInfo = new GameEventPayloads.MoveInfo(
            moveNumber, color
        );
        
        GameEventPayloads.OpponentMovedPayload payload = new GameEventPayloads.OpponentMovedPayload(
            moveInfo,
            capturedPositions,
            currentTurn
        );
        
        GameEvent event = new GameEvent(GameEvent.OPPONENT_MOVED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that their opponent has passed.
     * 
     * @param playerId the player to notify
     * @param moveNumber the move sequence number
     * @param consecutivePasses number of consecutive passes (2 triggers negotiation)
     * @param currentTurn whose turn it is now
     */
    public void notifyOpponentPassed(UUID playerId, int moveNumber, 
                                     int consecutivePasses, String currentTurn) {
        GameEventPayloads.OpponentPassedPayload payload = new GameEventPayloads.OpponentPassedPayload(
            moveNumber,
            consecutivePasses,
            currentTurn
        );
        
        GameEvent event = new GameEvent(GameEvent.OPPONENT_PASSED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that the game has ended (resignation).
     * 
     * @param playerId the player to notify
     * @param reason the reason for ending (RESIGNATION)
     * @param winner the winner's color
     * @param resignedBy nickname of the player who resigned
     */
    public void notifyGameEnded(UUID playerId, String reason, String winner, String resignedBy) {
        GameEventPayloads.GameEndedPayload payload = new GameEventPayloads.GameEndedPayload(
            reason,
            winner,
            resignedBy
        );
        
        GameEvent event = new GameEvent(GameEvent.GAME_ENDED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that the game has ended with final scoring.
     * 
     * @param playerId the player to notify
     * @param reason the reason for ending (SCORING_COMPLETE)
     * @param winner the winner's color
     * @param score detailed score breakdown
     */
    public void notifyGameEndedWithScore(UUID playerId, String reason, String winner, 
                                          GameEventPayloads.ScoreBreakdown score) {
        GameEventPayloads.GameEndedPayload payload = new GameEventPayloads.GameEndedPayload(
            reason,
            winner,
            null,
            score
        );
        
        GameEvent event = new GameEvent(GameEvent.GAME_ENDED, payload);
        sendToPlayer(playerId, event);
    }
    
    // ==================== NEGOTIATION NOTIFICATIONS ====================
    
    /**
     * Notifies a player that the negotiation (scoring) phase has started.
     * <p>
     * Sent after both players pass consecutively. Contains all chains
     * that need dead/alive determination.
     * </p>
     * 
     * @param playerId the player to notify
     * @param chains list of all chains with their suggested status
     * @param komi the komi value for scoring
     */
    public void notifyNegotiationStarted(UUID playerId, List<GameEventPayloads.ChainSuggestion> chains, 
                                          double komi) {
        GameEventPayloads.NegotiationStartedPayload payload = 
            new GameEventPayloads.NegotiationStartedPayload(chains, komi);
        
        GameEvent event = new GameEvent(GameEvent.NEGOTIATION_STARTED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that a chain's dead/alive status has been changed.
     * <p>
     * Called when either player marks a chain as dead or alive during negotiation.
     * Both players must accept for the score to be finalized.
     * </p>
     * 
     * @param playerId the player to notify
     * @param chainId the chain whose status changed
     * @param newStatus the new status (ALIVE/DEAD)
     * @param changedBy nickname of player who made the change
     * @param blackAccepted whether black player has accepted current state
     * @param whiteAccepted whether white player has accepted current state
     */
    public void notifyChainStatusChanged(UUID playerId, int chainId, String newStatus,
                                          String changedBy, boolean blackAccepted, 
                                          boolean whiteAccepted) {
        GameEventPayloads.ChainStatusChangedPayload payload = 
            new GameEventPayloads.ChainStatusChangedPayload(
                chainId, newStatus, changedBy, blackAccepted, whiteAccepted
            );
        
        GameEvent event = new GameEvent(GameEvent.CHAIN_STATUS_CHANGED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that someone has accepted the current score.
     * <p>
     * When both players accept, the game ends with final scoring.
     * </p>
     * 
     * @param playerId the player to notify
     * @param acceptedBy nickname of player who accepted
     * @param blackAccepted whether black has accepted
     * @param whiteAccepted whether white has accepted
     * @param currentScore current score breakdown
     */
    public void notifyScoreAccepted(UUID playerId, String acceptedBy, 
                                     boolean blackAccepted, boolean whiteAccepted,
                                     GameEventPayloads.ScoreBreakdown currentScore) {
        GameEventPayloads.ScoreAcceptedPayload payload = 
            new GameEventPayloads.ScoreAcceptedPayload(
                acceptedBy, blackAccepted, whiteAccepted, currentScore
            );
        
        GameEvent event = new GameEvent(GameEvent.SCORE_ACCEPTED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that the game has resumed from negotiation phase.
     * <p>
     * Called when a player chooses to resume the game instead of accepting the score.
     * The game returns to normal play.
     * </p>
     * 
     * @param playerId the player to notify
     * @param resumedByNickname nickname of player who resumed
     * @param currentTurn whose turn it is after resuming
     */
    public void notifyGameResumed(UUID playerId, String resumedByNickname, String currentTurn) {
        GameEventPayloads.GameResumedPayload payload = 
            new GameEventPayloads.GameResumedPayload(resumedByNickname, currentTurn);
        
        GameEvent event = new GameEvent(GameEvent.GAME_RESUMED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player about an error during negotiation.
     * <p>
     * Used to inform players about invalid operations during scoring phase.
     * </p>
     * 
     * @param playerId the player to notify
     * @param errorCode the error code (e.g., INVALID_CHAIN, NOT_YOUR_TURN)
     * @param message human-readable error message
     * @param chainId the chain ID involved (may be null)
     */
    public void notifyNegotiationError(UUID playerId, String errorCode, String message, 
                                        Integer chainId) {
        GameEventPayloads.NegotiationErrorPayload payload = 
            new GameEventPayloads.NegotiationErrorPayload(errorCode, message, chainId);
        
        GameEvent event = new GameEvent(GameEvent.NEGOTIATION_ERROR, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Sends an event to a specific player via WebSocket.
     * <p>
     * Uses the STOMP destination pattern {@code /topic/game/{playerId}}.
     * </p>
     * 
     * @param playerId the target player's ID
     * @param payload the event payload to send
     */
    private void sendToPlayer(UUID playerId, Object payload) {
        String destination = "/topic/game/" + playerId.toString();
        messagingTemplate.convertAndSend(destination, payload);
    }
}
