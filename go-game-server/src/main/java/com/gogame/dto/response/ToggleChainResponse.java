package com.gogame.dto.response;

/**
 * Response after toggling a chain's dead/alive status.
 */
public record ToggleChainResponse(
    int chainId,
    String newStatus,
    boolean blackAccepted,
    boolean whiteAccepted,
    String message
) {}
