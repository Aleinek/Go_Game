package com.gogame.websocket;

import java.util.List;
import java.util.UUID;

/**
 * Contains all payload record types for WebSocket game events.
 * <p>
 * Each record corresponds to a specific {@link GameEvent} type.
 * Payloads are serialized to JSON when sent via STOMP.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class GameEventPayloads {
    
    /**
     * Payload for GAME_STARTED event.
     * Sent when matchmaking pairs two players.
     * 
     * @param gameId the new game's UUID
     * @param yourColor assigned color ("BLACK" or "WHITE")
     * @param opponent information about the opponent
     * @param boardSize the game's board size
     */
    public record GameStartedPayload(
        UUID gameId,
        String yourColor,
        OpponentInfo opponent,
        int boardSize
    ) {}
    
    /**
     * Information about an opponent player.
     * 
     * @param nickname the opponent's display name
     */
    public record OpponentInfo(
        String nickname
    ) {}
    
    /**
     * Payload for OPPONENT_MOVED event.
     * Sent when the opponent places a stone.
     * 
     * @param move details about the move
     * @param capturedPositions positions where stones were captured
     * @param currentTurn whose turn it is now
     */
    public record OpponentMovedPayload(
        MoveInfo move,
        List<PositionInfo> capturedPositions,
        String currentTurn
    ) {}
    
    /**
     * Basic move information.
     * 
     * @param moveNumber the sequence number of this move
     * @param color the color of the stone placed
     */
    public record MoveInfo(
        int moveNumber,
        String color
    ) {}
    
    /**
     * Board position coordinates.
     * 
     * @param x the x-coordinate (column)
     * @param y the y-coordinate (row)
     */
    public record PositionInfo(
        int x,
        int y
    ) {}
    
    /**
     * Payload for OPPONENT_PASSED event.
     * 
     * @param moveNumber the move number of the pass
     * @param consecutivePasses total consecutive passes (2 triggers negotiation)
     * @param currentTurn whose turn it is now
     */
    public record OpponentPassedPayload(
        int moveNumber,
        int consecutivePasses,
        String currentTurn
    ) {}
    
    /**
     * Payload for GAME_ENDED event.
     * Sent on resignation or when scoring completes.
     * 
     * @param reason "RESIGNATION" or "SCORING_COMPLETE"
     * @param winner "BLACK" or "WHITE"
     * @param resignedBy nickname of resigning player (null if scoring)
     * @param score detailed score breakdown (null if resignation)
     */
    public record GameEndedPayload(
        String reason,
        String winner,
        String resignedBy,
        ScoreBreakdown score
    ) {
        /** Constructor for resignation case (no score). */
        public GameEndedPayload(String reason, String winner, String resignedBy) {
            this(reason, winner, resignedBy, null);
        }
    }
    
    // ==================== NEGOTIATION PHASE PAYLOADS ====================
    
    /**
     * Sent when negotiation phase starts (after two consecutive passes).
     */
    public record NegotiationStartedPayload(
        List<ChainSuggestion> chains,
        double komi
    ) {}
    
    /**
     * Information about a chain during negotiation.
     */
    public record ChainSuggestion(
        int chainId,
        List<PositionInfo> positions,
        String color,
        int liberties,
        String status  // "ALIVE" or "DEAD"
    ) {}
    
    /**
     * Sent when a player toggles a chain's dead/alive status.
     */
    public record ChainStatusChangedPayload(
        int chainId,
        String newStatus,  // "ALIVE" or "DEAD"
        String changedBy,  // "BLACK" or "WHITE"
        boolean blackAccepted,
        boolean whiteAccepted
    ) {}
    
    /**
     * Sent when a player accepts the current score.
     */
    public record ScoreAcceptedPayload(
        String acceptedBy,  // "BLACK" or "WHITE"
        boolean blackAccepted,
        boolean whiteAccepted,
        ScoreBreakdown currentScore
    ) {}
    
    /**
     * Sent when game resumes from negotiation phase.
     */
    public record GameResumedPayload(
        String resumedBy,   // Nickname of player who resumed
        String currentTurn  // "BLACK" or "WHITE" - who plays next
    ) {}
    
    /**
     * Detailed score breakdown for Japanese scoring.
     */
    public record ScoreBreakdown(
        int blackTerritory,
        int whiteTerritory,
        int blackPrisoners,      // Captured during game
        int whitePrisoners,      // Captured during game
        int blackDeadStones,     // Marked dead in negotiation
        int whiteDeadStones,     // Marked dead in negotiation
        double komi,
        double blackTotal,
        double whiteTotal,
        String winner,
        double scoreDifference
    ) {}
    
    /**
     * Error payload for invalid negotiation actions.
     */
    public record NegotiationErrorPayload(
        String errorCode,
        String message,
        Integer chainId
    ) {}
}
