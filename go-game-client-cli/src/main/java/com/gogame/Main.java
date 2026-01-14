package com.gogame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

            while ("IN_PROGRESS".equals(gameResponse.status()) || "NEGOTIATING".equals(gameResponse.status())) {
                
                // Obsługa fazy negocjacji
                if ("NEGOTIATING".equals(gameResponse.status())) {
                    handleNegotiationPhase(apiController, webSocketClient, gameId, playerId, playerColor);
                    gameResponse = apiController.fetchGameStatus(gameId);
                    
                    if ("FINISHED".equals(gameResponse.status())) {
                        System.out.println("\nGra zakończona po negocjacjach!");
                        break;
                    } else if ("IN_PROGRESS".equals(gameResponse.status())) {
                        System.out.println("\nGra wznowiona! Kontynuujemy rozgrywkę.");
                        continue;
                    }
                }
                
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
                            case GameEvent.NEGOTIATION_STARTED -> {
                                NegotiationStartedPayload negotiation = (NegotiationStartedPayload) event.payload();
                                System.out.println("\n=== FAZA NEGOCJACJI PUNKTACJI ===");
                                System.out.println("Obaj gracze spasowali. Rozpoczyna się negocjacja martwych kamieni.");
                                System.out.println("Komi (kompensacja dla białych): " + negotiation.komi());
                                gameResponse = apiController.fetchGameStatus(gameId);
                            }
                            case GameEvent.GAME_RESUMED -> {
                                GameResumedPayload resumed = (GameResumedPayload) event.payload();
                                System.out.println("\n=== GRA WZNOWIONA ===");
                                System.out.println(resumed.resumedBy() + " wznowił grę!");
                                System.out.println("Kolejka: " + resumed.currentTurn());
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
        } else if ("SCORING_COMPLETE".equals(ended.reason()) && ended.score() != null) {
            System.out.println("Powód: Zakończenie negocjacji punktacji");
            printScoreBreakdown(ended.score());
            System.out.println("\nZwycięzca: " + ended.winner());
        }
    }
    
    /**
     * Wyświetla szczegółowy rozkład punktacji.
     */
    private static void printScoreBreakdown(GameEndedPayload.ScoreBreakdown score) {
        System.out.println("\n--- SZCZEGÓŁOWY WYNIK ---");
        System.out.println("CZARNY:");
        System.out.println("  Terytorium: " + score.blackTerritory());
        System.out.println("  Jeńcy zdobyci w grze: " + score.blackPrisoners());
        System.out.println("  Martwe kamienie przeciwnika: " + score.whiteDeadStones());
        System.out.println("  SUMA: " + score.blackTotal());
        
        System.out.println("\nBIAŁY:");
        System.out.println("  Terytorium: " + score.whiteTerritory());
        System.out.println("  Jeńcy zdobyci w grze: " + score.whitePrisoners());
        System.out.println("  Martwe kamienie przeciwnika: " + score.blackDeadStones());
        System.out.println("  Komi: " + score.komi());
        System.out.println("  SUMA: " + score.whiteTotal());
        
        System.out.println("\nRóżnica: " + score.scoreDifference() + " punktów");
    }
    
    /**
     * Wyświetla szczegółowy rozkład punktacji (z ScoreResponse).
     */
    private static void printScoreBreakdown(ScoreResponse score) {
        System.out.println("\n--- SZCZEGÓŁOWY WYNIK ---");
        System.out.println("CZARNY:");
        System.out.println("  Terytorium: " + score.blackTerritory());
        System.out.println("  Jeńcy zdobyci w grze: " + score.blackPrisoners());
        System.out.println("  Martwe kamienie przeciwnika: " + score.whiteDeadStones());
        System.out.println("  SUMA: " + score.blackTotal());
        
        System.out.println("\nBIAŁY:");
        System.out.println("  Terytorium: " + score.whiteTerritory());
        System.out.println("  Jeńcy zdobyci w grze: " + score.whitePrisoners());
        System.out.println("  Martwe kamienie przeciwnika: " + score.blackDeadStones());
        System.out.println("  Komi: " + score.komi());
        System.out.println("  SUMA: " + score.whiteTotal());
        
        System.out.println("\nRóżnica: " + score.scoreDifference() + " punktów");
    }
    
    /**
     * Obsługuje fazę negocjacji punktacji.
     * Używa nieblokującego sprawdzania inputu, aby móc reagować na eventy WebSocket.
     */
    private static void handleNegotiationPhase(APIController apiController, GameWebSocketClient webSocketClient,
                                                UUID gameId, UUID playerId, String playerColor) {
        System.out.println("\n=== FAZA NEGOCJACJI ===");
        System.out.println("Oznacz martwe grupy kamieni lub zaakceptuj aktualny wynik.");
        System.out.println("Dostępne komendy:");
        System.out.println("  'chains' - pokaż listę łańcuchów kamieni");
        System.out.println("  'toggle <id>' - przełącz status łańcucha (martwy/żywy)");
        System.out.println("  'score' - pokaż aktualny wynik");
        System.out.println("  'accept' - zaakceptuj wynik");
        System.out.println("  'resume' - wznów grę (jeśli nie zgadzasz się z oznaczeniami)");
        System.out.println("  'help' - pokaż pomoc");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        try {
            boolean negotiating = true;
            boolean promptShown = false;
            
            while (negotiating) {
                // Sprawdź czy są eventy WebSocket (z krótkim timeoutem zamiast pollowania)
                GameEventWrapper event = webSocketClient.waitForGameEvent(200, TimeUnit.MILLISECONDS);
                
                if (event != null) {
                    switch (event.type()) {
                        case GameEvent.CHAIN_STATUS_CHANGED -> {
                            ChainStatusChangedPayload changed = (ChainStatusChangedPayload) event.payload();
                            System.out.println("\n[INFO] " + changed.changedBy() + " zmienił status łańcucha #" + 
                                changed.chainId() + " na " + changed.newStatus());
                            System.out.println("      Akceptacje - Czarny: " + changed.blackAccepted() + 
                                ", Biały: " + changed.whiteAccepted());
                            promptShown = false;
                        }
                        case GameEvent.SCORE_ACCEPTED -> {
                            ScoreAcceptedPayload accepted = (ScoreAcceptedPayload) event.payload();
                            System.out.println("\n[INFO] " + accepted.acceptedBy() + " zaakceptował wynik!");
                            if (accepted.currentScore() != null) {
                                System.out.println("      Aktualny wynik - Czarny: " + 
                                    accepted.currentScore().blackTotal() + ", Biały: " + 
                                    accepted.currentScore().whiteTotal());
                            }
                            if (accepted.blackAccepted() && accepted.whiteAccepted()) {
                                System.out.println("\n[INFO] Obaj gracze zaakceptowali! Gra zaraz się zakończy.");
                            }
                            promptShown = false;
                        }
                        case GameEvent.GAME_RESUMED -> {
                            GameResumedPayload resumed = (GameResumedPayload) event.payload();
                            System.out.println("\n[INFO] " + resumed.resumedBy() + " wznowił grę!");
                            System.out.println("      Kolejka: " + resumed.currentTurn());
                            negotiating = false;
                            continue; // Natychmiast wychodzimy z pętli
                        }
                        case GameEvent.GAME_ENDED -> {
                            GameEndedPayload ended = (GameEndedPayload) event.payload();
                            handleGameEnded(ended, playerColor, null);
                            negotiating = false;
                            continue; // Natychmiast wychodzimy z pętli
                        }
                    }
                }
                
                // Sprawdź czy użytkownik coś wpisał (nieblokująco)
                if (!negotiating) {
                    break;
                }
                
                if (!promptShown) {
                    System.out.print("\nNegocjacja> ");
                    System.out.flush();
                    promptShown = true;
                }
                
                // Sprawdź czy jest dostępny input od użytkownika
                if (reader.ready()) {
                    String command = reader.readLine();
                    if (command == null) {
                        continue;
                    }
                    command = command.trim().toLowerCase();
                    promptShown = false;
                    
                    if (command.equals("chains")) {
                        try {
                            NegotiationStateResponse state = apiController.getNegotiationState(gameId, playerId);
                            printChainsList(state);
                        } catch (Exception e) {
                            System.out.println("Błąd pobierania łańcuchów: " + e.getMessage());
                        }
                    } else if (command.startsWith("toggle ")) {
                        try {
                            int chainId = Integer.parseInt(command.substring(7).trim());
                            apiController.toggleChainStatus(gameId, playerId, chainId);
                            System.out.println("Zmieniono status łańcucha #" + chainId);
                        } catch (NumberFormatException e) {
                            System.out.println("Niepoprawny numer łańcucha. Użycie: toggle <id>");
                        } catch (Exception e) {
                            System.out.println("Błąd zmiany statusu: " + e.getMessage());
                        }
                    } else if (command.equals("score")) {
                        try {
                            ScoreResponse score = apiController.getScorePreview(gameId, playerId);
                            System.out.println("\n=== PODGLĄD WYNIKU ===");
                            printScoreBreakdown(score);
                        } catch (Exception e) {
                            System.out.println("Błąd pobierania wyniku: " + e.getMessage());
                        }
                    } else if (command.equals("accept")) {
                        try {
                            apiController.acceptScore(gameId, playerId);
                            System.out.println("Zaakceptowano wynik. Oczekiwanie na drugiego gracza...");
                        } catch (Exception e) {
                            System.out.println("Błąd akceptacji: " + e.getMessage());
                        }
                    } else if (command.equals("resume")) {
                        try {
                            apiController.resumePlaying(gameId, playerId);
                            System.out.println("Wznawianie gry...");
                            negotiating = false;
                        } catch (Exception e) {
                            System.out.println("Błąd wznawiania gry: " + e.getMessage());
                        }
                    } else if (command.equals("help")) {
                        System.out.println("\nDostępne komendy:");
                        System.out.println("  chains       - wyświetl listę łańcuchów kamieni z ich statusami");
                        System.out.println("  toggle <id>  - przełącz status łańcucha (martwy <-> żywy)");
                        System.out.println("  score        - wyświetl aktualny podgląd wyniku");
                        System.out.println("  accept       - zaakceptuj aktualny stan martwych kamieni i zakończ grę");
                        System.out.println("  resume       - odrzuć negocjacje i wznów grę");
                        System.out.println("  help         - pokaż tę pomoc");
                    } else if (command.isEmpty()) {
                        // Pusta linia - nic nie rób
                    } else {
                        System.out.println("Nieznana komenda. Wpisz 'help' aby zobaczyć dostępne komendy.");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Błąd odczytu inputu: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Przerwano oczekiwanie: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("Błąd podczas negocjacji: " + e.getMessage());
        }
    }
    
    /**
     * Wyświetla listę łańcuchów kamieni podczas negocjacji.
     */
    private static void printChainsList(NegotiationStateResponse state) {
        System.out.println("\n=== ŁAŃCUCHY KAMIENI ===");
        System.out.println("Akceptacje - Czarny: " + state.blackAccepted() + ", Biały: " + state.whiteAccepted());
        System.out.println();
        
        if (state.chains() == null || state.chains().isEmpty()) {
            System.out.println("Brak łańcuchów do wyświetlenia.");
            return;
        }
        
        for (NegotiationStateResponse.ChainInfoDto chain : state.chains()) {
            String statusSymbol = "DEAD".equals(chain.status()) ? "☠" : "✓";
            String colorName = "BLACK".equals(chain.color()) ? "CZARNY" : "BIAŁY";
            
            System.out.printf("#%d [%s] %s - %s (oddechów: %d, kamieni: %d)%n",
                chain.chainId(),
                statusSymbol,
                colorName,
                chain.status(),
                chain.liberties(),
                chain.positions() != null ? chain.positions().size() : 0
            );
            
            // Pokaż pozycje kamieni
            if (chain.positions() != null && !chain.positions().isEmpty()) {
                StringBuilder positions = new StringBuilder("    Pozycje: ");
                for (int i = 0; i < chain.positions().size() && i < 10; i++) {
                    NegotiationStateResponse.PositionDto pos = chain.positions().get(i);
                    if (i > 0) positions.append(", ");
                    positions.append("(").append(pos.x() + 1).append(",").append(pos.y() + 1).append(")");
                }
                if (chain.positions().size() > 10) {
                    positions.append("... i ").append(chain.positions().size() - 10).append(" więcej");
                }
                System.out.println(positions);
            }
        }
        
        System.out.println("\nUżyj 'toggle <id>' aby zmienić status łańcucha (np. 'toggle 1')");
    }
}