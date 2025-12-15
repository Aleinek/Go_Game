package com.gogame;

import java.util.UUID;

// http://gogame.adamkulwicki.pl:8080/

public class Main {

    private static final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    public static void main(String[] args) {

        UUID gameId;
        APIController apiController = new APIController(SERVER_URL);

        String playerName = CLIController.getPlayerName();

        try {
            
            System.out.println("Rejestracja...");
            PlayerResponse player = apiController.registerPlayer(playerName);
            System.out.println("Witaj w grze, " + player.nickname() + "!");
            int sz = CLIController.getBoardSize();

            GameResponse game = apiController.joinGame(player.id(), sz);

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
            } 

            CLIController.printGameStartingMessage(game);
            //

        } catch (RuntimeException e) {
            System.out.println("Wystapil blad!");
            e.printStackTrace();
        }
    }
     
}