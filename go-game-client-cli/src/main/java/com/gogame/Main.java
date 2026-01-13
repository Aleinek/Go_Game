package com.gogame;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.gogame.model.Board;
import com.gogame.model.MoveType;
import com.gogame.printer.BoardPrinter;
import com.gogame.controller.APIController;
import com.gogame.controller.CLIController;
import com.gogame.dto.*;
import com.gogame.dto.websocket.*;
import com.gogame.websocket.GameWebSocketClient;
import com.gogame.websocket.GameWebSocketClient.GameEventWrapper;

/**
 * Główna klasa klienta CLI do gry Go.
 * Używa WebSocketów do otrzymywania powiadomień o zdarzeniach gry (zamiast pollingu).
 */
public class Main {

    private static final String SERVER_URL = "http://adamkulwicki.gogame:8080";
    
    public static void main(String[] args) {
        
        UUID playerId = null;
        UUID gameId = null;
        int boardSize = 0;
        String myColor = null;
        
        APIController apiController = new APIController(SERVER_URL);
        GameWebSocketClient webSocketClient = new GameWebSocketClient(SERVER_URL);

        String playerName = CLIController.getPlayerName();

        // Blok rejestracji gracza i dołączania do gry
        try { 
            System.out.println("Rejestracja...");
            PlayerResponse player = apiController.registerPlayer(playerName);
            playerId = player.id();
            System.out.println("Witaj w grze, " + player.nickname() + "!");
            
            // Połącz z WebSocket PRZED dołączeniem do kolejki
            System.out.println("Łączenie z serwerem...");
            webSocketClient.connect(playerId);
            
            boardSize = CLIController.getBoardSize();

            GameResponse game = apiController.joinGame(player.id(), boardSize);

            if ("WAITING".equals(game.status())) {
                System.out.println("Jesteś w kolejce.");
                System.out.println("Czekanie na przeciwnika...");
                
                // Czekamy na zdarzenie GAME_STARTED przez WebSocket (zamiast pollingu)
                GameStartedPayload gameStarted = null;
                while (gameStarted == null) {
                    gameStarted = webSocketClient.waitForGameStart(2, TimeUnit.SECONDS);
                    if (gameStarted == null) {
                        System.out.print(".");
                    }
                }
                
                gameId = gameStarted.gameId();
                myColor = gameStarted.yourColor();
                boardSize = gameStarted.boardSize();
                
                System.out.println("\nZnaleziono przeciwnika: " + gameStarted.opponent().nickname());
                System.out.println("Grasz kolorem: " + (myColor.equals("BLACK") ? "CZARNYM" : "BIAŁYM"));
                
            } else if ("IN_PROGRESS".equals(game.status())) {
                gameId = game.id();
                myColor = playerName.equals(game.blackPlayer().nickname()) ? "BLACK" : "WHITE";
            }

            // Pobierz pełny stan gry
            GameResponse gameResponse = apiController.fetchGameStatus(gameId);
            CLIController.printGameStartingMessage(gameResponse);            

        } catch (RuntimeException e) {
            System.out.println("Wystąpił błąd!");
            e.printStackTrace();
            webSocketClient.disconnect();
            return;
        } catch (Exception e) {
            System.out.println("Błąd połączenia WebSocket!");
            e.printStackTrace();
            return;
        }

        // Główna pętla gry
        try {
            GameResponse gameResponse = apiController.fetchGameStatus(gameId);
            final String playerColor = myColor;

            while ("IN_PROGRESS".equals(gameResponse.status())) {
                
                boolean itIsMyTurn = playerColor.equals(gameResponse.currentTurn());
                
                if (itIsMyTurn) {
                    // Przed wykonaniem ruchu rysujemy planszę 
                    if (gameResponse.lastMove() != null) {
                        System.out.println("\nPrzeciwnik wykonał ruch (" + 
                            gameResponse.lastMove().x() + "," + gameResponse.lastMove().y() + ")");
                    } else if (gameResponse.moveCount() == 0) {
                        System.out.println("Rozpoczynasz grę!");
                    }
                    
                    BoardResponseDTO boardResponseDTO = apiController.fetchBoard(gameId);
                    BoardPrinter.printBoard(new Board(boardResponseDTO), 
                        gameResponse.blackPlayer().capturedStones(),
                        gameResponse.whitePlayer().capturedStones(),
                        boardResponseDTO.whiteTerritory(),
                        boardResponseDTO.blackTerritory());

                    MoveType moveType = CLIController.getMoveType();

                    if (moveType == MoveType.NORMAL_MOVE) {
                        while (true) {
                            try {
                                int x = CLIController.getXFromPlayerInput(boardSize);
                                int y = CLIController.getYFromPlayerInput(boardSize);
                                MoveResponse moveResponse = apiController.makeMove(gameId, playerId, x, y);
                                if (!moveResponse.success()) {
                                    throw new IllegalArgumentException();
                                }
                                System.out.println("Poprawnie wykonano ruch (" + (x+1) + "," + (y+1) + ")!");
                                break;
                            } catch (Exception e) {
                                System.out.println("Podany ruch był niepoprawny - spróbuj ponownie");
                            }
                        }
                    } else if (moveType == MoveType.PASS) {
                        apiController.pass(gameId, playerId);
                        System.out.println("Spasowałeś.");
                    } else { // MoveType.RESIGN
                        CLIController.printResignMessage(
                            playerColor.equals("BLACK") ? gameResponse.whitePlayer() : gameResponse.blackPlayer());
                        apiController.resign(gameId, playerId);
                        webSocketClient.disconnect();
                        return;
                    }
                    
                    // Po wykonaniu ruchu pobieramy stan i rysujemy planszę
                    gameResponse = apiController.fetchGameStatus(gameId);
                    boardResponseDTO = apiController.fetchBoard(gameId);
                    BoardPrinter.printBoard(new Board(boardResponseDTO), 
                        gameResponse.blackPlayer().capturedStones(),
                        gameResponse.whitePlayer().capturedStones(),
                        boardResponseDTO.whiteTerritory(),
                        boardResponseDTO.blackTerritory());
                    
                    if ("IN_PROGRESS".equals(gameResponse.status())) {
                        System.out.print("Czekam na ruch rywala...");
                    }

                } else {
                    // Nie nasza tura - czekamy na zdarzenie WebSocket (zamiast pollingu!)
                    GameEventWrapper event = webSocketClient.waitForGameEvent(30, TimeUnit.SECONDS);
                    
                    if (event != null) {
                        switch (event.type()) {
                            case GameEvent.OPPONENT_MOVED -> {
                                OpponentMovedPayload moved = (OpponentMovedPayload) event.payload();
                                System.out.println("\nPrzeciwnik wykonał ruch!");
                                // Odśwież stan gry
                                gameResponse = apiController.fetchGameStatus(gameId);
                            }
                            case GameEvent.OPPONENT_PASSED -> {
                                OpponentPassedPayload passed = (OpponentPassedPayload) event.payload();
                                System.out.println("\nPrzeciwnik spasował! (Pasy pod rząd: " + 
                                    passed.consecutivePasses() + ")");
                                gameResponse = apiController.fetchGameStatus(gameId);
                            }
                            case GameEvent.GAME_ENDED -> {
                                GameEndedPayload ended = (GameEndedPayload) event.payload();
                                handleGameEnded(ended, playerColor, gameResponse);
                                webSocketClient.disconnect();
                                return;
                            }
                        }
                    } else {
                        // Timeout - sprawdź stan przez REST API
                        gameResponse = apiController.fetchGameStatus(gameId);
                    }
                }
                
                // Sprawdź czy gra się zakończyła
                if ("RESIGNED".equals(gameResponse.status())) {
                    CLIController.printOpponentResignedMessage(
                        playerColor.equals("BLACK") ? gameResponse.whitePlayer() : gameResponse.blackPlayer());
                    break;
                } else if ("FINISHED".equals(gameResponse.status())) {
                    System.out.println("\nGra zakończona!");
                    break;
                }
            }
            
        } catch (Exception e) {
            System.out.println("Wystąpił błąd!");
            e.printStackTrace();
        } finally {
            webSocketClient.disconnect();
        }
    }
    
    /**
     * Obsługuje zdarzenie zakończenia gry otrzymane przez WebSocket.
     */
    private static void handleGameEnded(GameEndedPayload ended, String myColor, GameResponse gameResponse) {
        System.out.println("\n=== GRA ZAKOŃCZONA ===");
        
        if ("RESIGNATION".equals(ended.reason())) {
            System.out.println("Powód: " + ended.resignedBy() + " zrezygnował");
            System.out.println("Zwycięzca: " + ended.winner());
        } else if ("TWO_PASSES".equals(ended.reason())) {
            System.out.println("Powód: Obaj gracze spasowali");
            if (ended.winner() != null) {
                System.out.println("Zwycięzca: " + ended.winner());
            } else {
                System.out.println("Remis!");
            }
        }
    }
}