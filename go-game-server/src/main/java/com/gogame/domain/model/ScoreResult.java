package com.gogame.domain.model;

/**
 * Final scoring result for a Go game using Japanese rules.
 * Score = Territory + Prisoners (captured during game) + Dead stones - Komi (for white)
 */
public class ScoreResult {
    private final int blackTerritory;
    private final int whiteTerritory;
    private final int blackPrisoners;      // Stones captured by black during game
    private final int whitePrisoners;      // Stones captured by white during game
    private final int blackDeadStones;     // Black stones marked dead in negotiation
    private final int whiteDeadStones;     // White stones marked dead in negotiation
    private final double komi;
    private final double blackTotal;
    private final double whiteTotal;
    private final String winner;
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
