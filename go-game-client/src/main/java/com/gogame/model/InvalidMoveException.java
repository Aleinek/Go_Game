package com.gogame.model;

/**
 * Exception thrown when an invalid move is attempted in a Go game.
 */
public class InvalidMoveException extends RuntimeException {

    /**
     * Error codes for invalid moves
     */
    public enum ErrorCode {
        POSITION_OCCUPIED("Position is already occupied"),
        OUT_OF_BOUNDS("Position is outside the board boundaries"),
        NOT_YOUR_TURN("It is not your turn to play"),
        SUICIDE_MOVE("Move would result in immediate capture (suicide)"),
        KO_VIOLATION("Move violates the Ko rule"),
        GAME_NOT_IN_PROGRESS("Game is not in progress"),
        INVALID_POSITION("Invalid position coordinates");

        private final String description;

        ErrorCode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private final ErrorCode errorCode;
    private final Position position;

    public InvalidMoveException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.position = null;
    }

    public InvalidMoveException(ErrorCode errorCode, Position position) {
        super(String.format("%s at position (%d, %d)", 
            errorCode.getDescription(), 
            position.getX(), 
            position.getY()));
        this.errorCode = errorCode;
        this.position = position;
    }

    public InvalidMoveException(String message) {
        super(message);
        this.errorCode = null;
        this.position = null;
    }

    public InvalidMoveException(String message, Position position) {
        super(String.format("%s at position (%d, %d)", message, position.getX(), position.getY()));
        this.errorCode = null;
        this.position = position;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Position getPosition() {
        return position;
    }
}
