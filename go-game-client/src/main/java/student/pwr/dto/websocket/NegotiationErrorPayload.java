package student.pwr.dto.websocket;

/**
 * Payload for NEGOTIATION_ERROR event.
 * <p>
 * Sent when a negotiation action is invalid.
 * </p>
 * 
 * @param errorCode the error code
 * @param message human-readable error message
 * @param chainId optional chain ID related to the error
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record NegotiationErrorPayload(
    String errorCode,
    String message,
    Integer chainId
) {}
