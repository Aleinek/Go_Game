package com.gogame;

import java.util.UUID;

import com.gogame.model.Board;
import com.gogame.model.MoveType;
import com.gogame.dto.*;

// http://gogame.adamkulwicki.pl:8080/

public class Main {

    private static final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    public static void main(String[] args) {
        
        UUID playerId = null;
        UUID gameId = null;
        int boardSize = 0;
        APIController apiController = new APIController(SERVER_URL);

        String playerName = CLIController.getPlayerName();

        // blok try-catch odpowiada za zaincjowalizowanie rozgrywki i uzyskanie id rozgrywki
        try { 
            System.out.println("Rejestracja...");
            PlayerResponse player = apiController.registerPlayer(playerName);
            playerId = player.id();
            System.out.println("Witaj w grze, " + player.nickname() + "!");
            boardSize = CLIController.getBoardSize();

            GameResponse game = apiController.joinGame(player.id(), boardSize);

            if ("WAITING".equals(game.status())) {
                System.out.println("Jestes w kolejce.");
                System.out.println("Czekanie na przeciwnika...");
                
                WaitingStatus waitingStatus = apiController.checkWaitingStatus(game.id());
                
                while ("WAITING".equals(waitingStatus.status())) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    
                    System.out.print("."); 
                    waitingStatus = apiController.checkWaitingStatus(game.id());
                }

                gameId = waitingStatus.gameId();
                game = apiController.fetchGameStatus(gameId);
            } else if("IN_PROGRESS".equals(game.status())) {
                gameId = game.id();
            }

            CLIController.printGameStartingMessage(game);            

        } catch (RuntimeException e) {
            System.out.println("Wystapil blad!");
            e.printStackTrace();
            return;
        }

        
        try {
            GameResponse gameResponse = apiController.fetchGameStatus(gameId);
            String myColor = (playerName.equals(gameResponse.blackPlayer().nickname()) ? "BLACK" : "WHITE");

            while ("IN_PROGRESS".equals(gameResponse.status())) {
                
                boolean itIsMyTurn = (myColor.equals(gameResponse.currentTurn()) ? true : false);
                if (itIsMyTurn) {
                    // przed wykonaniem ruchu rysujemy plansze 
                    if(gameResponse.lastMove() != null)
                        System.out.println(" Przeciwnik wykonal ruch (" + gameResponse.lastMove().x() + ',' + gameResponse.lastMove().y() + ")");
                    else 
                        System.out.println("Rozpoczynasz gre!");
                    BoardResponseDTO boardResponseDTO = apiController.fetchBoard(gameId);
                    BoardPrinter.printBoard(new Board(boardResponseDTO), 
                    gameResponse.blackPlayer().capturedStones(),
                    gameResponse.whitePlayer().capturedStones());

                    MoveType moveType = CLIController.getMoveType();

                    if(moveType == MoveType.NORMAL_MOVE) {
                        while (true) {
                            // probujemy wykonac ruch
                            try {
                                int x = CLIController.getXFromPlayerInput(boardSize);
                                int y = CLIController.getYFromPlayerInput(boardSize);
                                MoveResponse moveResponse = apiController.makeMove(gameId, playerId, x, y);
                                if(!moveResponse.success()) {
                                    throw new IllegalArgumentException();
                                }
                                System.out.println("Poprawnie wykonano ruch (" + (x+1) + "," + (y+1) + ")!");
                                break;
                            } catch(Exception e) {
                                System.out.println("Podany ruch byl niepoprawny - sprobuj ponownie");
                            }
                        }
                    } else if(moveType == MoveType.PASS) {
                        MoveResponse moveResponse = apiController.pass(gameId, playerId);
                    } else { //moveType == MoveType.RESIGN
                        GameResponse resignResponse = apiController.resign(gameId, playerId);
                        break;
                    }
                    boardResponseDTO = apiController.fetchBoard(gameId);
                    // po wykonaniu ruchu rysujemy plansze
                    BoardPrinter.printBoard(new Board(boardResponseDTO), 
                                            gameResponse.blackPlayer().capturedStones(),
                                            gameResponse.whitePlayer().capturedStones());
                    System.out.print("Czekam na ruch rywala...");

                } else {
                    // nie nasza tura wiec spimy jedna sekunde
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }      
                gameResponse = apiController.fetchGameStatus(gameId);
            }        
        } catch (Exception e) {
            System.out.println("Wystapil blad!");
            e.printStackTrace();
            return;
        }
    
    }
}