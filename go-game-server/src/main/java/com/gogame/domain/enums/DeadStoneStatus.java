package com.gogame.domain.enums;

/**
 * Status of a chain during the scoring negotiation phase.
 * <p>
 * Players can mark chains as:
 * <ul>
 *   <li>ALIVE - Chain remains on the board and controls territory</li>
 *   <li>DEAD - Chain is removed; stones count as prisoners for opponent</li>
 * </ul>
 * </p>
 * <p>
 * Both players must agree on all chain statuses before finalizing the score.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public enum DeadStoneStatus {
    /** Chain is alive and will remain on the board. */
    ALIVE,
    /** Chain is marked as dead and counts as prisoners. */
    DEAD
}
