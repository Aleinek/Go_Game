package com.gogame.service;

import com.gogame.websocket.GameEvent;
import com.gogame.websocket.GameEventPayloads;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GameNotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public GameNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
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
    
    public void notifyGameEnded(UUID playerId, String reason, String winner, String resignedBy) {
        GameEventPayloads.GameEndedPayload payload = new GameEventPayloads.GameEndedPayload(
            reason,
            winner,
            resignedBy
        );
        
        GameEvent event = new GameEvent(GameEvent.GAME_ENDED, payload);
        sendToPlayer(playerId, event);
    }
    
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
     * Notifies a player that negotiation phase has started.
     */
    public void notifyNegotiationStarted(UUID playerId, List<GameEventPayloads.ChainSuggestion> chains, 
                                          double komi) {
        GameEventPayloads.NegotiationStartedPayload payload = 
            new GameEventPayloads.NegotiationStartedPayload(chains, komi);
        
        GameEvent event = new GameEvent(GameEvent.NEGOTIATION_STARTED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player that a chain's dead/alive status has changed.
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
     * Notifies a player that someone accepted the score.
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
     * Notifies a player that game resumed from negotiation.
     */
    public void notifyGameResumed(UUID playerId, String resumedByNickname, String currentTurn) {
        GameEventPayloads.GameResumedPayload payload = 
            new GameEventPayloads.GameResumedPayload(resumedByNickname, currentTurn);
        
        GameEvent event = new GameEvent(GameEvent.GAME_RESUMED, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Notifies a player about a negotiation error.
     */
    public void notifyNegotiationError(UUID playerId, String errorCode, String message, 
                                        Integer chainId) {
        GameEventPayloads.NegotiationErrorPayload payload = 
            new GameEventPayloads.NegotiationErrorPayload(errorCode, message, chainId);
        
        GameEvent event = new GameEvent(GameEvent.NEGOTIATION_ERROR, payload);
        sendToPlayer(playerId, event);
    }
    
    /**
     * Wysyła zdarzenie do konkretnego gracza na kanał /topic/game/{playerId}
     */
    private void sendToPlayer(UUID playerId, Object payload) {
        String destination = "/topic/game/" + playerId.toString();
        messagingTemplate.convertAndSend(destination, payload);
    }
}
