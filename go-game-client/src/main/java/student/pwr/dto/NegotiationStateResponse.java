package student.pwr.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for negotiation (scoring) state response from the server.
 * <p>
 * Contains chain information for dead/alive marking and score preview.
 * </p>
 * 
 * @param gameId the game's unique identifier
 * @param chains list of all chains on the board
 * @param blackAccepted whether black has accepted
 * @param whiteAccepted whether white has accepted
 * @param komi the komi value
 * @param scorePreview projected score based on current markings
 * @param message optional status message
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record NegotiationStateResponse(
    UUID gameId,
    List<ChainInfoDto> chains,
    boolean blackAccepted,
    boolean whiteAccepted,
    double komi,
    ScorePreviewDto scorePreview,
    String message
) {
    public record ChainInfoDto(
        int chainId,
        List<PositionDto> positions,
        String color,
        int liberties,
        String status
    ) {}
    
    public record PositionDto(
        int x,
        int y
    ) {}
    
    public record ScorePreviewDto(
        int blackTerritory,
        int whiteTerritory,
        int blackPrisoners,
        int whitePrisoners,
        int blackDeadStones,
        int whiteDeadStones,
        double komi,
        double blackTotal,
        double whiteTotal,
        String winner,
        double scoreDifference
    ) {}
}
