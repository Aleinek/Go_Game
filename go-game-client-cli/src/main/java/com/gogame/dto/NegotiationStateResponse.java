package com.gogame.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response containing the current negotiation state.
 */
public record NegotiationStateResponse(
    UUID gameId,
    List<ChainInfoDto> chains,
    boolean blackAccepted,
    boolean whiteAccepted,
    double komi,
    ScorePreviewDto scorePreview,
    String message
) {
    public record ChainInfoDto(
        int chainId,
        List<PositionDto> positions,
        String color,
        int liberties,
        String status
    ) {}
    
    public record PositionDto(
        int x,
        int y
    ) {}
    
    public record ScorePreviewDto(
        int blackTerritory,
        int whiteTerritory,
        int blackPrisoners,
        int whitePrisoners,
        int blackDeadStones,
        int whiteDeadStones,
        double komi,
        double blackTotal,
        double whiteTotal,
        String winner,
        double scoreDifference
    ) {}
}
