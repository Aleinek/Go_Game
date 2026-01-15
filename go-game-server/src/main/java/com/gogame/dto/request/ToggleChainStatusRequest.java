package com.gogame.dto.request;

/**
 * Request DTO to toggle the dead/alive status of a chain during negotiation.
 * <p>
 * During the negotiation phase, players can mark chains as DEAD or ALIVE.
 * Toggling a chain resets both players' acceptance status.
 * </p>
 * 
 * @param chainId the ID of the chain to toggle
 * @author Go Game Team
 * @version 1.0
 */
public record ToggleChainStatusRequest(
    int chainId
) {}
