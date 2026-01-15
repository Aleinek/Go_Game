package com.gogame.domain.model;

import java.util.List;
import java.util.UUID;

import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;

/**
 * Represents a Go game instance with all its state.
 * <p>
 * A Game contains:
 * <ul>
 *   <li>Two players (black and white)</li>
 *   <li>A board with the current stone positions</li>
 *   <li>Move history</li>
 *   <li>Game status (IN_PROGRESS, NEGOTIATING, FINISHED, RESIGNED)</li>
 *   <li>Scoring data (komi, negotiation state, final score)</li>
 * </ul>
 * </p>
 * <p>
 * The game lifecycle:
 * <ol>
 *   <li>Created when matchmaking pairs two players</li>
 *   <li>IN_PROGRESS - players take turns placing stones</li>
 *   <li>NEGOTIATING - both passed, determining dead stones</li>
 *   <li>FINISHED - score calculated and winner determined</li>
 * </ol>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class Game {
    public UUID id;
    public Board board;
    public Player blackPlayer;
    public Player whitePlayer;
    public StoneColor currentTurn;
    public GameStatus status;
    public List<Move> moves;
    public int consecutivePasses;
    
    // Negotiation phase fields
    public static final double DEFAULT_KOMI = 6.5;
    public double komi;
    public NegotiationState negotiationState;
    public ScoreResult finalScore;
    public StoneColor lastPlayerBeforeNegotiation;

    public Game(Player blackPlayer, Player whitePlayer, Board board) {
        this.id = UUID.randomUUID();
        this.board = board;
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currentTurn = StoneColor.BLACK;
        this.status = GameStatus.IN_PROGRESS;
        this.moves = new java.util.ArrayList<>();
        this.consecutivePasses = 0;
        this.komi = DEFAULT_KOMI;
        this.negotiationState = null;
        this.finalScore = null;
        this.lastPlayerBeforeNegotiation = null;
    }

    public void resign(Player player) {
        if (status == GameStatus.IN_PROGRESS) {
            status = GameStatus.RESIGNED;
        }
    }

    public Player getCurrentPlayer() {
        return (currentTurn == StoneColor.BLACK) ? blackPlayer : whitePlayer;
    }

    public boolean isGameOver() {
        return status == GameStatus.FINISHED || status == GameStatus.RESIGNED;
    }
    
    public boolean isNegotiating() {
        return status == GameStatus.NEGOTIATING;
    }
    
    public Player getWinner() {
        if (status == GameStatus.RESIGNED) {
            return (getCurrentPlayer() == blackPlayer) ? whitePlayer : blackPlayer;
        }
        if (status == GameStatus.FINISHED && finalScore != null) {
            String winner = finalScore.getWinner();
            if ("BLACK".equals(winner)) return blackPlayer;
            if ("WHITE".equals(winner)) return whitePlayer;
        }
        return null;
    }

    public StoneColor getCurrentTurn() {
        return currentTurn;
    }

    public void switchTurn() {
        currentTurn = (currentTurn == StoneColor.BLACK) ? StoneColor.WHITE : StoneColor.BLACK;
    }

    /**
     * Starts the negotiation phase after two consecutive passes.
     */
    public void startNegotiation() {
        if (consecutivePasses >= 2) {
            this.lastPlayerBeforeNegotiation = currentTurn;
            this.status = GameStatus.NEGOTIATING;
            this.negotiationState = new NegotiationState();
        }
    }

    /**
     * Resumes playing from negotiation phase.
     * The player who requested resume gets the turn.
     */
    public void resumePlaying(StoneColor requestingPlayerColor) {
        if (status == GameStatus.NEGOTIATING) {
            this.status = GameStatus.IN_PROGRESS;
            this.currentTurn = requestingPlayerColor;
            this.consecutivePasses = 0;
            this.negotiationState = null;
        }
    }

    /**
     * Ends the game with final score.
     */
    public void finishWithScore(ScoreResult score) {
        this.finalScore = score;
        this.status = GameStatus.FINISHED;
    }

    void checkGameEnd() {
        if (consecutivePasses >= 2) {
            status = GameStatus.FINISHED;
        }
    }
}
