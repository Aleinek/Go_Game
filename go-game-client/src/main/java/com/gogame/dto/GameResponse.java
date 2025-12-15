package com.gogame.dto;

import java.util.UUID;

public record GameResponse(
    UUID id, 
    String status, 
    int boardSize, 
    String currentTurn, 
    Integer moveCount,
    GamePlayer blackPlayer, 
    GamePlayer whitePlayer, 
    String message,
    GameMove lastMove
) {}