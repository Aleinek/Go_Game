package com.gogame.dto.response;

/**
 * Response DTO returned after toggling a chain's dead/alive status.
 * <p>
 * When a chain is toggled, both players' acceptance is reset
 * since the proposed score has changed.
 * </p>
 * 
 * @param chainId the ID of the chain that was toggled
 * @param newStatus the new status ("DEAD" or "ALIVE")
 * @param blackAccepted whether black has accepted (always false after toggle)
 * @param whiteAccepted whether white has accepted (always false after toggle)
 * @param message optional status message
 * @author Go Game Team
 * @version 1.0
 */
public record ToggleChainResponse(
    int chainId,
    String newStatus,
    boolean blackAccepted,
    boolean whiteAccepted,
    String message
) {}
