package com.gogame.websocket;

import java.time.Instant;

/**
 * Represents a WebSocket event sent to players via STOMP.
 * <p>
 * Each event has:
 * <ul>
 *   <li>type - one of the defined event type constants</li>
 *   <li>payload - event-specific data (see {@link GameEventPayloads})</li>
 *   <li>timestamp - when the event was created</li>
 * </ul>
 * </p>
 * <p>
 * Events are sent to {@code /topic/game/{playerId}} for each player.
 * </p>
 * 
 * @param type the event type constant
 * @param payload event-specific data object
 * @param timestamp when the event was created
 * @author Go Game Team
 * @version 1.0
 */
public record GameEvent(
    String type,
    Object payload,
    Instant timestamp
) {
    /**
     * Creates an event with the current timestamp.
     * 
     * @param type the event type
     * @param payload the event payload
     */
    public GameEvent(String type, Object payload) {
        this(type, payload, Instant.now());
    }
    
    /** Game started - match found, contains opponent info and assigned color. */
    public static final String GAME_STARTED = "GAME_STARTED";
    /** Opponent placed a stone - contains move and any captures. */
    public static final String OPPONENT_MOVED = "OPPONENT_MOVED";
    /** Opponent passed their turn. */
    public static final String OPPONENT_PASSED = "OPPONENT_PASSED";
    /** Game has ended - resignation or scoring complete. */
    public static final String GAME_ENDED = "GAME_ENDED";
    
    // Negotiation phase events
    /** Both players passed - entering scoring phase. */
    public static final String NEGOTIATION_STARTED = "NEGOTIATION_STARTED";
    /** A chain's dead/alive status was changed. */
    public static final String CHAIN_STATUS_CHANGED = "CHAIN_STATUS_CHANGED";
    /** A player accepted the current score proposal. */
    public static final String SCORE_ACCEPTED = "SCORE_ACCEPTED";
    /** Game resumed from negotiation - back to normal play. */
    public static final String GAME_RESUMED = "GAME_RESUMED";
    /** Error during negotiation action. */
    public static final String NEGOTIATION_ERROR = "NEGOTIATION_ERROR";
}
