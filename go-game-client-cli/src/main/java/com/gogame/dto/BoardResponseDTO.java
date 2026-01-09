package com.gogame.dto;

import java.util.List;
import java.util.UUID;

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