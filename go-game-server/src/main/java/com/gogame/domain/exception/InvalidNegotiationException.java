package com.gogame.domain.exception;

/**
 * Exception thrown when an invalid action is attempted during scoring negotiation.
 */
public class InvalidNegotiationException extends RuntimeException {

    public enum ErrorCode {
        GAME_NOT_IN_NEGOTIATION("Game is not in negotiation phase"),
        CHAIN_NOT_FOUND("Chain not found"),
        INVALID_DEAD_MARKING("Invalid dead stone marking - chain appears to be alive"),
        CHAIN_HAS_TOO_MANY_LIBERTIES("Cannot mark chain as dead - has more than 3 liberties"),
        CHAIN_NOT_SURROUNDED("Cannot mark chain as dead - not fully surrounded by opponent"),
        CHAIN_IS_OWN_COLOR("Cannot mark chain as dead - surrounded by own color"),
        NOT_YOUR_GAME("You are not a player in this game"),
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
