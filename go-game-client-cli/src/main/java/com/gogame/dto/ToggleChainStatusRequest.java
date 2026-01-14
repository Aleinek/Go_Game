package com.gogame.dto;

/**
 * Request to toggle dead/alive status of a chain during negotiation.
 */
public record ToggleChainStatusRequest(
    int chainId
) {}
