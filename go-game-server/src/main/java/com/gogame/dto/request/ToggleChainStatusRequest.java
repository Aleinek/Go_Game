package com.gogame.dto.request;

/**
 * Request to toggle dead/alive status of a chain during negotiation.
 */
public record ToggleChainStatusRequest(
    int chainId
) {}
