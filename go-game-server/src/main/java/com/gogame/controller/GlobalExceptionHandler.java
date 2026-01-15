package com.gogame.controller;

import com.gogame.domain.exception.GameNotFoundException;
import com.gogame.domain.exception.InvalidMoveException;
import com.gogame.domain.exception.InvalidNegotiationException;
import com.gogame.domain.exception.PlayerNotFoundException;
import com.gogame.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the Go Game API.
 * <p>
 * Converts domain exceptions into appropriate HTTP responses with
 * error codes and messages for clients to handle.
 * </p>
 * 
 * @author Go Game Team
 * @version 1.0
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles invalid move attempts (occupied position, ko, suicide, etc.).
     * 
     * @param ex the exception with error details
     * @return BAD_REQUEST (400) with error code and message
     */
    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMove(InvalidMoveException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode() != null ? ex.getErrorCode().toString() : "INVALID_MOVE",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Handles invalid negotiation actions (wrong phase, invalid chain marking).
     * 
     * @param ex the exception with error details
     * @return BAD_REQUEST (400) with error code and message
     */
    @ExceptionHandler(InvalidNegotiationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNegotiation(InvalidNegotiationException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getErrorCode() != null ? ex.getErrorCode().toString() : "INVALID_NEGOTIATION",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Handles requests for non-existent games.
     * 
     * @param ex the exception with game ID
     * @return NOT_FOUND (404) with error message
     */
    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGameNotFound(GameNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("GAME_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles requests with non-existent player IDs.
     * 
     * @param ex the exception with player ID
     * @return NOT_FOUND (404) with error message
     */
    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePlayerNotFound(PlayerNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("PLAYER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    /**
     * Handles validation errors for request parameters.
     * 
     * @param ex the exception with validation message
     * @return BAD_REQUEST (400) with validation error
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    /**
     * Catches all unhandled exceptions as a fallback.
     * 
     * @param ex the unexpected exception
     * @return INTERNAL_SERVER_ERROR (500) with generic message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred: " + ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
