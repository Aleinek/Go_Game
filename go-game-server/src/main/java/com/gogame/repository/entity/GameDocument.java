package com.gogame.repository.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MongoDB document representing a saved Go game.
 * <p>
 * Stores complete game state including:
 * <ul>
 *   <li>Game metadata (id, status, board size, timestamps)</li>
 *   <li>Player information (both players embedded)</li>
 *   <li>Complete move history (all moves embedded)</li>
 *   <li>Scoring data (komi, final score, winner)</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@Document(collection = "games")
public class GameDocument {
    
    @Id
    private UUID id;
    
    private int boardSize;
    private String status;
    private String currentTurn;
    
    private PlayerDocument blackPlayer;
    private PlayerDocument whitePlayer;
    
    private List<MoveDocument> moves = new ArrayList<>();
    
    private int consecutivePasses;
    private double komi;
    
    // Final game result
    private String winner;
    private Double blackScore;
    private Double whiteScore;
    
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    private Instant finishedAt;

    public GameDocument() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(String currentTurn) {
        this.currentTurn = currentTurn;
    }

    public PlayerDocument getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(PlayerDocument blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public PlayerDocument getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(PlayerDocument whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public List<MoveDocument> getMoves() {
        return moves;
    }

    public void setMoves(List<MoveDocument> moves) {
        this.moves = moves;
    }

    public int getConsecutivePasses() {
        return consecutivePasses;
    }

    public void setConsecutivePasses(int consecutivePasses) {
        this.consecutivePasses = consecutivePasses;
    }

    public double getKomi() {
        return komi;
    }

    public void setKomi(double komi) {
        this.komi = komi;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public Double getBlackScore() {
        return blackScore;
    }

    public void setBlackScore(Double blackScore) {
        this.blackScore = blackScore;
    }

    public Double getWhiteScore() {
        return whiteScore;
    }

    public void setWhiteScore(Double whiteScore) {
        this.whiteScore = whiteScore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }
}
