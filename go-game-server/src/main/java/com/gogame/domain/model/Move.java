package com.gogame.domain.model;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a single move in a Go game.
 * <p>
 * A move can be either:
 * <ul>
 *   <li>Stone placement - player places a stone at a position</li>
 *   <li>Pass - player passes their turn</li>
 * </ul>
 * </p>
 * <p>
 * Moves are recorded in sequence with a move number and track
 * any stones captured as a result of the move.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class Move {
    /** Unique identifier for this move. */
    public UUID id;
    /** The player who made this move. */
    public Player player;
    /** Position where stone was placed (null for pass moves). */
    public Position position;
    /** Sequential move number in the game. */
    public int moveNumber;
    /** True if this move is a pass. */
    public boolean isPass;
    /** Number of opponent stones captured by this move. */
    public int capturedStones;
    /** Timestamp when the move was made. */
    public LocalDate timestamp;

    public Move(Player player, Position position, int moveNumber) {
        this.id = UUID.randomUUID();
        this.player = player;
        this.position = position;
        this.moveNumber = moveNumber;
        this.timestamp = LocalDate.now();
    }

    public static Move pass(Player player, int moveNumber) {
        Move move = new Move(player, null, moveNumber);
        move.isPass = true;
        return move;
    }

    public boolean isPass() {
        return isPass;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public int getMoveNumber() {
        return moveNumber;
    }
    
    public int getCapturedStones() {
        return capturedStones;
    }
}
