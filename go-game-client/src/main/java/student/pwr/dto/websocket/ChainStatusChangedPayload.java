package student.pwr.dto.websocket;

/**
 * Payload for CHAIN_STATUS_CHANGED event.
 * <p>
 * Sent when a player toggles a chain's status (ALIVE <-> DEAD).
 * </p>
 * 
 * @param chainId the chain whose status changed
 * @param newStatus "ALIVE" or "DEAD"
 * @param changedBy "BLACK" or "WHITE"
 * @param blackAccepted whether black has accepted
 * @param whiteAccepted whether white has accepted
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record ChainStatusChangedPayload(
    int chainId,
    String newStatus,       // "ALIVE" lub "DEAD"
    String changedBy,       // "BLACK" lub "WHITE"
    boolean blackAccepted,
    boolean whiteAccepted
) {}
