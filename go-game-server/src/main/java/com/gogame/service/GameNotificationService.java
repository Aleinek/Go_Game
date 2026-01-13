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
    
    /**
     * Wysyła zdarzenie do konkretnego gracza na kanał /topic/game/{playerId}
     */
    private void sendToPlayer(UUID playerId, Object payload) {
        String destination = "/topic/game/" + playerId.toString();
        messagingTemplate.convertAndSend(destination, payload);
    }
}
