package com.gogame.domain.enums;

/**
 * Represents the possible states of a Go game.
 * <p>
 * Game lifecycle:
 * <pre>
 * WAITING_FOR_PLAYERS → IN_PROGRESS ↔ NEGOTIATING → FINISHED
 *                              ↓
 *                          RESIGNED
 * </pre>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public enum GameStatus {
    /** Waiting for a second player to join (matchmaking). */
    WAITING_FOR_PLAYERS,
    /** Game is active, players take turns. */
    IN_PROGRESS,
    /** Both players passed, determining dead stones for scoring. */
    NEGOTIATING,
    /** Game completed with final score calculated. */
    FINISHED,
    /** A player resigned, opponent wins. */
    RESIGNED
}
