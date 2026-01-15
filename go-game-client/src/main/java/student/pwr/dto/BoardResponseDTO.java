package student.pwr.dto;

import java.util.List;
import java.util.UUID;

/**
 * DTO for board state response from the server.
 * <p>
 * Contains all information needed to render the board:
 * stone positions, captured counts, and territory.
 * </p>
 * 
 * @param gameId the game's unique identifier
 * @param size the board size (9, 13, or 19)
 * @param moveNumber current move number
 * @param stones list of all stones on the board
 * @param blackCaptured stones captured by black
 * @param whiteCaptured stones captured by white
 * @param whiteTerritory white's territory count
 * @param blackTerritory black's territory count
 * @param neutralTerritory neutral points (dame)
 * @author Go Game Team - PWR
 * @version 1.0
 */
public record BoardResponseDTO(
    UUID gameId,
    int size,
    int moveNumber,
    List<StoneDTO> stones, 
    int blackCaptured,
    int whiteCaptured,
    int whiteTerritory,
    int blackTerritory,
    int neutralTerritory
) {}