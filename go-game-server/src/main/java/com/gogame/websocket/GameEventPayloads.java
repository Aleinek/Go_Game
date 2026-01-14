package com.gogame.websocket;

import java.util.List;
import java.util.UUID;

public class GameEventPayloads {
    
    public record GameStartedPayload(
        UUID gameId,
        String yourColor,
        OpponentInfo opponent,
        int boardSize
    ) {}
    
    public record OpponentInfo(
        String nickname
    ) {}
    
    public record OpponentMovedPayload(
        MoveInfo move,
        List<PositionInfo> capturedPositions,
        String currentTurn
    ) {}
    
    public record MoveInfo(
        int moveNumber,
        String color
    ) {}
    
    public record PositionInfo(
        int x,
        int y
    ) {}
    
    public record OpponentPassedPayload(
        int moveNumber,
        int consecutivePasses,
        String currentTurn
    ) {}
    
    public record GameEndedPayload(
        String reason,
        String winner,
        String resignedBy,
        ScoreBreakdown score
    ) {
        // Constructor for backward compatibility (resignation case)
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
