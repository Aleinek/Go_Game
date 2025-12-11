package com.gogame.dto.request;

public record JoinGameRequest(
    int boardSize
) {
    public JoinGameRequest {
        if (boardSize != 9 && boardSize != 13 && boardSize != 19) {
            throw new IllegalArgumentException("Board size must be 9, 13, or 19");
        }
    }
}
