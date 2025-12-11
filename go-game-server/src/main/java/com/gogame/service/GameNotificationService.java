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
        sendToUser(playerId, "/queue/game", event);
    }
    
    public void notifyOpponentMoved(UUID playerId, int moveNumber, int x, int y, 
                                    String color, List<GameEventPayloads.PositionInfo> capturedPositions,
                                    String currentTurn) {
        GameEventPayloads.MoveInfo moveInfo = new GameEventPayloads.MoveInfo(
            moveNumber, x, y, color
        );
        
        GameEventPayloads.OpponentMovedPayload payload = new GameEventPayloads.OpponentMovedPayload(
            moveInfo,
            capturedPositions,
            currentTurn
        );
        
        GameEvent event = new GameEvent(GameEvent.OPPONENT_MOVED, payload);
        sendToUser(playerId, "/queue/game", event);
    }
    
    public void notifyOpponentPassed(UUID playerId, int moveNumber, 
                                     int consecutivePasses, String currentTurn) {
        GameEventPayloads.OpponentPassedPayload payload = new GameEventPayloads.OpponentPassedPayload(
            moveNumber,
            consecutivePasses,
            currentTurn
        );
        
        GameEvent event = new GameEvent(GameEvent.OPPONENT_PASSED, payload);
        sendToUser(playerId, "/queue/game", event);
    }
    
    public void notifyGameEnded(UUID playerId, String reason, String winner, String resignedBy) {
        GameEventPayloads.GameEndedPayload payload = new GameEventPayloads.GameEndedPayload(
            reason,
            winner,
            resignedBy
        );
        
        GameEvent event = new GameEvent(GameEvent.GAME_ENDED, payload);
        sendToUser(playerId, "/queue/game", event);
    }
    
    private void sendToUser(UUID userId, String destination, Object payload) {
        messagingTemplate.convertAndSendToUser(
            userId.toString(),
            destination,
            payload
        );
    }
}
