package com.gogame.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Move {
    public UUID id;
    public Player player;
    public Position position;
    public int moveNumber;
    public boolean isPass;
    public int capturedStones;
    public LocalDate timestamp;

    public Move(Player player, Position position, int moveNumber) {
        this.id = UUID.randomUUID();
        this.player = player;
        this.position = position;
        this.moveNumber = moveNumber;
        this.timestamp = LocalDate.now();
    }

    public static Move pass(Player player, int moveNumber) {
        Move move = new Move(player, null, moveNumber);
        move.isPass = true;
        return move;
    }

    public boolean isPass() {
        return isPass;
    }
}
