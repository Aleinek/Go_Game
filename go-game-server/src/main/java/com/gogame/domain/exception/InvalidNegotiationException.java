package com.gogame.domain.exception;

/**
 * Exception thrown when an invalid action is attempted during the scoring negotiation phase.
 * <p>
 * Negotiation has specific rules:
 * <ul>
 *   <li>Only players in the game can participate</li>
 *   <li>Game must be in NEGOTIATING state</li>
 *   <li>Only valid chains can be marked</li>
 *   <li>Cannot re-accept after changing a marking</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class InvalidNegotiationException extends RuntimeException {

    /**
     * Error codes for different types of negotiation errors.
     * Each code includes a human-readable description.
     */
    public enum ErrorCode {
        /** Game is not currently in the negotiation phase. */
        GAME_NOT_IN_NEGOTIATION("Game is not in negotiation phase"),
        /** The specified chain ID does not exist. */
        CHAIN_NOT_FOUND("Chain not found"),
        /** Attempted to mark a chain that appears alive as dead. */
        INVALID_DEAD_MARKING("Invalid dead stone marking - chain appears to be alive"),
        /** Chain is not surrounded and cannot be marked dead. */
        CHAIN_NOT_SURROUNDED("Cannot mark chain as dead - not fully surrounded by opponent"),
        /** Chain is surrounded by friendly stones. */
        CHAIN_IS_OWN_COLOR("Cannot mark chain as dead - surrounded by own color"),
        /** Player is not a participant in this game. */
        NOT_YOUR_GAME("You are not a player in this game"),
        /** Player has already accepted the score. */
        ALREADY_ACCEPTED("You have already accepted the score");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;
    private final Integer chainId;

    public InvalidNegotiationException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.chainId = null;
    }

    public InvalidNegotiationException(ErrorCode errorCode, int chainId) {
        super(String.format("%s (chain ID: %d)", errorCode.getDescription(), chainId));
        this.errorCode = errorCode;
        this.chainId = chainId;
    }

    public InvalidNegotiationException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.chainId = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Integer getChainId() {
        return chainId;
    }
}
