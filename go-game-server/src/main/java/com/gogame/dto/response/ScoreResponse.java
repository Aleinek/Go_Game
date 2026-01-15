package com.gogame.dto.response;

/**
 * Response DTO with final score breakdown using Japanese rules.
 * <p>
 * Score calculation:
 * <ul>
 *   <li>Black = blackTerritory + blackPrisoners + whiteDeadStones</li>
 *   <li>White = whiteTerritory + whitePrisoners + blackDeadStones + komi</li>
 * </ul>
 * </p>
 * 
 * @param blackTerritory territory controlled by black
 * @param whiteTerritory territory controlled by white
 * @param blackPrisoners stones captured by black during game
 * @param whitePrisoners stones captured by white during game
 * @param blackDeadStones black stones marked dead
 * @param whiteDeadStones white stones marked dead
 * @param komi compensation for white
 * @param blackTotal black's final score
 * @param whiteTotal white's final score
 * @param winner "BLACK", "WHITE", or "TIE"
 * @param scoreDifference absolute score difference
 * @param message optional status message
 * @author Go Game Team
 * @version 1.0
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
