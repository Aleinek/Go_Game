package com.gogame.domain.model;

/**
 * Final scoring result for a Go game using Japanese rules.
 * <p>
 * Japanese scoring formula:
 * <ul>
 *   <li>Black's score = Black territory + prisoners captured by black + white dead stones</li>
 *   <li>White's score = White territory + prisoners captured by white + black dead stones + komi</li>
 * </ul>
 * </p>
 * <p>
 * Komi is compensation for white playing second (typically 6.5 points).
 * The 0.5 ensures no ties in official games.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class ScoreResult {
    /** Territory points controlled by black. */
    private final int blackTerritory;
    /** Territory points controlled by white. */
    private final int whiteTerritory;
    /** Stones captured by black during the game (prisoners). */
    private final int blackPrisoners;
    /** Stones captured by white during the game (prisoners). */
    private final int whitePrisoners;
    /** Black stones marked dead in negotiation (count for white). */
    private final int blackDeadStones;
    /** White stones marked dead in negotiation (count for black). */
    private final int whiteDeadStones;
    /** Komi compensation for white. */
    private final double komi;
    /** Black's total score. */
    private final double blackTotal;
    /** White's total score (includes komi). */
    private final double whiteTotal;
    /** Winner: "BLACK", "WHITE", or "TIE". */
    private final String winner;
    /** Absolute difference between scores. */
    private final double scoreDifference;

    public ScoreResult(int blackTerritory, int whiteTerritory, 
                       int blackPrisoners, int whitePrisoners,
                       int blackDeadStones, int whiteDeadStones, 
                       double komi) {
        this.blackTerritory = blackTerritory;
        this.whiteTerritory = whiteTerritory;
        this.blackPrisoners = blackPrisoners;
        this.whitePrisoners = whitePrisoners;
        this.blackDeadStones = blackDeadStones;
        this.whiteDeadStones = whiteDeadStones;
        this.komi = komi;

        // Japanese scoring:
        // Black's score = Black territory + prisoners captured by black + white dead stones
        // White's score = White territory + prisoners captured by white + black dead stones + komi
        this.blackTotal = blackTerritory + blackPrisoners + whiteDeadStones;
        this.whiteTotal = whiteTerritory + whitePrisoners + blackDeadStones + komi;

        this.scoreDifference = Math.abs(blackTotal - whiteTotal);
        if (blackTotal > whiteTotal) {
            this.winner = "BLACK";
        } else if (whiteTotal > blackTotal) {
            this.winner = "WHITE";
        } else {
            this.winner = "TIE";
        }
    }

    public int getBlackTerritory() {
        return blackTerritory;
    }

    public int getWhiteTerritory() {
        return whiteTerritory;
    }

    public int getBlackPrisoners() {
        return blackPrisoners;
    }

    public int getWhitePrisoners() {
        return whitePrisoners;
    }

    public int getBlackDeadStones() {
        return blackDeadStones;
    }

    public int getWhiteDeadStones() {
        return whiteDeadStones;
    }

    public double getKomi() {
        return komi;
    }

    public double getBlackTotal() {
        return blackTotal;
    }

    public double getWhiteTotal() {
        return whiteTotal;
    }

    public String getWinner() {
        return winner;
    }

    public double getScoreDifference() {
        return scoreDifference;
    }

    @Override
    public String toString() {
        return String.format(
            "ScoreResult{Black: %.1f (territory=%d, prisoners=%d, deadOpponent=%d), " +
            "White: %.1f (territory=%d, prisoners=%d, deadOpponent=%d, komi=%.1f), Winner=%s by %.1f}",
            blackTotal, blackTerritory, blackPrisoners, whiteDeadStones,
            whiteTotal, whiteTerritory, whitePrisoners, blackDeadStones, komi,
            winner, scoreDifference
        );
    }
}
