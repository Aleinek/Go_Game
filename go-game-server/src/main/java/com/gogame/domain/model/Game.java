package com.gogame.domain.model;

import java.util.List;
import java.util.UUID;

import com.gogame.domain.enums.GameStatus;
import com.gogame.domain.enums.StoneColor;

public class Game {
    public UUID id;
    public Board board;
    public Player blackPlayer;
    public Player whitePlayer;
    public StoneColor currentTurn;
    public GameStatus status;
    public List<Move> moves;
    public int consecutivePasses;

    public Game(Player blackPlayer, Player whitePlayer, int boardSize, Board board) {
        this.id = UUID.randomUUID();
        this.board = board;
        this.blackPlayer = blackPlayer;
        this.whitePlayer = whitePlayer;
        this.currentTurn = StoneColor.BLACK;
        this.status = GameStatus.IN_PROGRESS;
        this.moves = new java.util.ArrayList<>();
        this.consecutivePasses = 0;
    }

    public MoveResult makeMove(Position position) {
        if (status != GameStatus.IN_PROGRESS) {
            return new MoveResult(false);
        }

        Player currentPlayer = (currentTurn == StoneColor.BLACK) ? blackPlayer : whitePlayer;
        Move move;
        if (position == null) {
            move = Move.pass(currentPlayer, moves.size() + 1);
            consecutivePasses++;
            if (consecutivePasses >= 2) {
                status = GameStatus.FINISHED;
                return new MoveResult(true);
            }
        } else {
            board.placeStone(position, currentTurn);
            move = new Move(currentPlayer, position, moves.size() + 1);
            consecutivePasses = 0;
        }

        moves.add(move);
        currentTurn = (currentTurn == StoneColor.BLACK) ? StoneColor.WHITE : StoneColor.BLACK;
        return new MoveResult(true);
    }

    public MoveResult pass() {
        return makeMove(null);
    }

    public void resign(Player player) {
        if (status == GameStatus.IN_PROGRESS) {
            status = GameStatus.RESIGNED;
        }
    }

    public Player getCurrentPlayer() {
        return (currentTurn == StoneColor.BLACK) ? blackPlayer : whitePlayer;
    }

    public boolean isGameOver() {
        return status != GameStatus.IN_PROGRESS;
    }
    
    public Player getWinner() {
        if (status == GameStatus.RESIGNED) {
            return (getCurrentPlayer() == blackPlayer) ? whitePlayer : blackPlayer;
        }
        return null;
    }

    public StoneColor getCurrentTurn() {
        return currentTurn;
    }

    void switchTurn() {
        currentTurn = (currentTurn == StoneColor.BLACK) ? StoneColor.WHITE : StoneColor.BLACK;
    }

    void checkGameEnd() {
        if (consecutivePasses >= 2) {
            status = GameStatus.FINISHED;
        }
    }
}
