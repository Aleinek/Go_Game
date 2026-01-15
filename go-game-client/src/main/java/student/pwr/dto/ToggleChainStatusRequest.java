package student.pwr.dto;

/**
 * DTO to toggle the dead/alive status of a chain during negotiation.
 * <p>
 * Toggling a chain resets both players' acceptance status.
 * </p>
 * 
 * @param chainId the ID of the chain to toggle
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record ToggleChainStatusRequest(
    int chainId
) {}
