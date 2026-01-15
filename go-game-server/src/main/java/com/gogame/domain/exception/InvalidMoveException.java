package com.gogame.domain.exception;

import com.gogame.domain.model.Position;

/**
 * Exception thrown when an invalid move is attempted in a Go game.
 * <p>
 * Go has strict rules about valid moves. This exception captures
 * various violation types with specific error codes.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
public class InvalidMoveException extends RuntimeException {

    /**
     * Error codes for different types of invalid moves.
     * Each code includes a human-readable description.
     */
    public enum ErrorCode {
        /** Attempting to place a stone on an occupied intersection. */
        POSITION_OCCUPIED("Position is already occupied"),
        /** Attempting to place a stone outside the board boundaries. */
        OUT_OF_BOUNDS("Position is outside the board boundaries"),
        /** Player attempting to move out of turn. */
        NOT_YOUR_TURN("It is not your turn to play"),
        /** Move would result in immediate capture with no captures. */
        SUICIDE_MOVE("Move would result in immediate capture (suicide)"),
        /** Move would recreate previous board position (Ko). */
        KO_VIOLATION("Move violates the Ko rule"),
        /** Game is not in IN_PROGRESS state. */
        GAME_NOT_IN_PROGRESS("Game is not in progress"),
        /** Invalid position coordinates provided. */
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
    
    public InvalidMoveException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.position = null;
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
