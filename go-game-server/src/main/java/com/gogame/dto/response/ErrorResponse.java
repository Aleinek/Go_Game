package com.gogame.dto.response;

import java.time.Instant;

/**
 * Response DTO for error conditions.
 * <p>
 * Provides a structured error response with:
 * <ul>
 *   <li>Error type/code</li>
 *   <li>Human-readable message</li>
 *   <li>Timestamp of when the error occurred</li>
 * </ul>
 * </p>
 * 
 * @param error the error type or code
 * @param message detailed error description
 * @param timestamp when the error occurred
 * @author Go Game Team
 * @version 1.0
 */
public record ErrorResponse(
    String error,
    String message,
    Instant timestamp
) {
    public ErrorResponse(String error, String message) {
        this(error, message, Instant.now());
    }
}
