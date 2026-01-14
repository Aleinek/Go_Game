package com.gogame.dto.response;

/**
 * Response with final score breakdown using Japanese rules.
 */
public record ScoreResponse(
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
    double scoreDifference,
    String message
) {}
