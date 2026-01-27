package student.pwr.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import student.pwr.dto.BoardResponseDTO;
import student.pwr.dto.GameResponse;
import student.pwr.dto.StoneDTO;
import student.pwr.dto.MoveResponse;
import student.pwr.dto.NegotiationStateResponse;
import student.pwr.dto.websocket.*;
import student.pwr.utils.AlertUtils;
import student.pwr.websocket.GameWebSocketClient;
import student.pwr.websocket.GameWebSocketClient.GameEventWrapper;

import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import student.pwr.dto.ScoreResponse;
import student.pwr.dto.MovesListDTO;
import javafx.scene.control.Alert;

/**
 * FXML controller for the game board view.
 * <p>
 * Manages:
 * <ul>
 *   <li>Drawing and updating the Go board</li>
 *   <li>Handling player moves (click to place stone)</li>
 *   <li>Pass, resign, and negotiation actions</li>
 *   <li>WebSocket event processing for real-time updates</li>
 *   <li>Score display and territory visualization</li>
 * </ul>
 * </p>
 * <p>
 * Uses an event-driven game loop that listens for WebSocket events
 * rather than polling the server.
 * </p>
 * 
 * @author Go Game Team - PWR
 * @version 1.0
 */
public class GameController {

    @FXML private Pane gameHeader;
    @FXML private Pane gamePane;
    @FXML private Label statusLabel;
    @FXML private Label blackCapturedLabel;
    @FXML private Label blackTerritoryLabel;
    @FXML private Label whiteCapturedLabel;
    @FXML private Label whiteTerritoryLabel;
    
    @FXML private Button passButton;
    @FXML private Button resumeButton;
    @FXML private Button acceptButton;
    @FXML private Button resignButton;
    @FXML private Button reviewButton;
    
    // Navigation controls for move review
    @FXML private HBox navigationPanel;
    @FXML private Button firstMoveButton;
    @FXML private Button prevMoveButton;
    @FXML private Button nextMoveButton;
    @FXML private Button lastMoveButton;
    @FXML private Button liveButton;
    @FXML private Label moveCountLabel;
    @FXML private Label reviewWarningLabel;
    
    private APIController apiController;
    private GameWebSocketClient webSocketClient;
    private GameResponse currentGame;
    private UUID myPlayerId;
    private String myColor; 
    private boolean isMyTurn = false;
    private int boardSize;
    private double cellSize;
    private final double BOARD_PADDING = 40.0;
    
    // Negotiation state
    private NegotiationStateResponse currentNegotiation;
    private boolean isNegotiating = false;
    
    // Game loop control
    private volatile boolean gameLoopRunning = false;
    
    // Move history for review mode
    private List<MovesListDTO.MoveInfo> moveHistory = new ArrayList<>();
    private int reviewMoveIndex = -1; // -1 = live mode, 0+ = review mode
    private boolean isInReviewMode = false;
    private String[][] reviewBoardState;
    
    // Callback for returning to menu after game ends
    private Runnable onGameEnded;

    @FXML
    public void initialize() {
        System.out.println("Kontroler gry zainicjalizowany");
    }
    
    public void setOnGameEnded(Runnable callback) {
        this.onGameEnded = callback;
    }

    public void initGame(GameResponse game, APIController api, UUID playerId, GameWebSocketClient wsClient, String color) {
        this.apiController = api;
        this.webSocketClient = wsClient;
        this.currentGame = game;
        this.myPlayerId = playerId;
        this.myColor = color;
        
        // Fallback jeśli kolor nie został przekazany
        if (this.myColor == null) {
            if (game.blackPlayer() != null && game.blackPlayer().id().equals(playerId)) {
                this.myColor = "BLACK";
            } else {
                this.myColor = "WHITE";
            }
        }

        // Print URLs for debugging
        String baseUrl = api.getServerURL();
        System.out.println("--- DEBUG LINKS ---");
        System.out.println("Game Status: " + baseUrl + "/api/games/" + game.id());
        System.out.println("Board Status: " + baseUrl + "/api/games/" + game.id() + "/board");
        System.out.println("WebSocket connected: " + (wsClient != null && wsClient.isConnected()));
        System.out.println("-------------------");

        this.boardSize = game.boardSize();
        this.reviewBoardState = new String[boardSize][boardSize];
        drawBoardGrid();
        
        // Wyczyść kolejkę eventów przed startem pętli
        if (webSocketClient != null) {
            webSocketClient.clearQueues();
        }
        
        // Start game loop z WebSocket
        startWebSocketGameLoop();
    }

    private void drawBoardGrid() {
        gamePane.getChildren().clear();
        
        double width = gamePane.getPrefWidth();
        double height = gamePane.getPrefHeight();
        double boardWidth = width - 2 * BOARD_PADDING;
        double boardHeight = height - 2 * BOARD_PADDING;
        
        this.cellSize = boardWidth / (boardSize - 1);

        // Draw background
        Rectangle bg = new Rectangle(width, height, Color.web("#427bbb"));
        gamePane.getChildren().add(bg);

        // Draw grid lines
        for (int i = 0; i < boardSize; i++) {
            // Vertical lines
            Line vLine = new Line(
                BOARD_PADDING + i * cellSize, BOARD_PADDING, 
                BOARD_PADDING + i * cellSize, height - BOARD_PADDING
            );
            
            // Horizontal lines
            Line hLine = new Line(
                BOARD_PADDING, BOARD_PADDING + i * cellSize, 
                width - BOARD_PADDING, BOARD_PADDING + i * cellSize
            );
            
            gamePane.getChildren().addAll(vLine, hLine);
        }

        // Add transparent click detectors
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                Circle clickArea = new Circle(cellSize / 2.5);
                clickArea.setFill(Color.TRANSPARENT);
                clickArea.setCenterX(BOARD_PADDING + x * cellSize);
                clickArea.setCenterY(BOARD_PADDING + y * cellSize);
                
                final int finalX = x;
                final int finalY = y;
                
                clickArea.setOnMouseClicked(e -> handleBoardClick(finalX, finalY));
                gamePane.getChildren().add(clickArea);
            }
        }
    }

    private void handleBoardClick(int x, int y) {
        if (isNegotiating) {
             if (currentNegotiation == null) {
                 return;
             }
             
             Integer targetChainId = null;
             if (currentNegotiation.chains() != null) {
                 for(NegotiationStateResponse.ChainInfoDto chain : currentNegotiation.chains()) {
                     for(NegotiationStateResponse.PositionDto pos : chain.positions()) {
                         if(pos.x() == x && pos.y() == y) {
                             targetChainId = chain.chainId();
                             break;
                         }
                     }
                     if(targetChainId != null) break;
                 }
             }
             
             if(targetChainId != null) {
                 final int chainId = targetChainId;
                 new Thread(() -> {
                     try {
                         NegotiationStateResponse newState = apiController.toggleChainStatus(currentGame.id(), myPlayerId, chainId);
                         this.currentNegotiation = newState; // Optimistic update
                         Platform.runLater(this::refreshGameState);
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }).start();
             }
             return;
        }

        if (!isMyTurn) {
            System.out.println("Nie Twoja tura!");
            return;
        }

        System.out.println("Kliknięto: " + x + ", " + y);
        
        // Try to make a move in background
        new Thread(() -> {
            try {
                MoveResponse response = apiController.makeMove(currentGame.id(), myPlayerId, x, y);
                if (response.success()) {
                    System.out.println("Ruch poprawny!");
                    Platform.runLater(() -> {
                        isMyTurn = false;
                        statusLabel.setText("Opponent's turn...");
                    });
                    // Refresh game state immediately after own move
                    refreshGameState();
                } else {
                    Platform.runLater(() -> AlertUtils.showAlert("Error", "Invalid move!"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert("Error", "Communication error: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Event-driven game loop using WebSocket events.
     * Replaces polling with blocking wait for WebSocket events.
     */
    private void startWebSocketGameLoop() {
        gameLoopRunning = true;
        
        Task<Void> gameLoopTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Initial state refresh
                refreshGameState();
                
                while (gameLoopRunning) {
                    // Check if game is finished
                    String status = currentGame.status();
                    if ("RESIGNED".equals(status) || "FINISHED".equals(status)) {
                        Platform.runLater(() -> handleGameEnd(currentGame));
                        break;
                    }
                    
                    // If it's my turn, just wait for events (my own actions trigger refreshes)
                    // If it's opponent's turn, wait for WebSocket event
                    if (!isMyTurn || isNegotiating) {
                        // Wait for WebSocket event (with timeout for fallback)
                        GameEventWrapper event = webSocketClient.waitForGameEvent(30, TimeUnit.SECONDS);
                        
                        if (event != null) {
                            handleWebSocketEvent(event);
                        } else {
                            // Timeout - fallback refresh via REST
                            System.out.println("WebSocket timeout, falling back to REST refresh");
                            refreshGameState();
                        }
                    } else {
                        // It's my turn - just poll for events without blocking
                        GameEventWrapper event = webSocketClient.pollGameEvent();
                        if (event != null) {
                            handleWebSocketEvent(event);
                        }
                        Thread.sleep(100); // Small delay to prevent busy loop
                    }
                }
                return null;
            }
        };

        Thread thread = new Thread(gameLoopTask);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Handles incoming WebSocket events and updates UI accordingly.
     */
    private void handleWebSocketEvent(GameEventWrapper event) {
        System.out.println("WebSocket event received: " + event.type());
        
        switch (event.type()) {
            case GameEvent.OPPONENT_MOVED -> {
                OpponentMovedPayload moved = (OpponentMovedPayload) event.payload();
                System.out.println("Opponent moved! Turn: " + moved.currentTurn());
                // Return to live mode when opponent moves
                if (isInReviewMode) {
                    Platform.runLater(this::returnToLiveMode);
                }
                refreshGameState();
            }
            case GameEvent.OPPONENT_PASSED -> {
                OpponentPassedPayload passed = (OpponentPassedPayload) event.payload();
                System.out.println("Opponent passed! Consecutive passes: " + passed.consecutivePasses());
                Platform.runLater(() -> 
                    statusLabel.setText("Opponent passed (" + passed.consecutivePasses() + " passes)")
                );
                refreshGameState();
            }
            case GameEvent.NEGOTIATION_STARTED -> {
                NegotiationStartedPayload negotiation = (NegotiationStartedPayload) event.payload();
                System.out.println("Negotiation started!");
                refreshGameState();
            }
            case GameEvent.CHAIN_STATUS_CHANGED -> {
                ChainStatusChangedPayload changed = (ChainStatusChangedPayload) event.payload();
                System.out.println("Chain " + changed.chainId() + " status changed to: " + changed.newStatus());
                refreshGameState();
            }
            case GameEvent.SCORE_ACCEPTED -> {
                ScoreAcceptedPayload accepted = (ScoreAcceptedPayload) event.payload();
                System.out.println("Score accepted by: " + accepted.acceptedBy());
                refreshGameState();
            }
            case GameEvent.GAME_RESUMED -> {
                GameResumedPayload resumed = (GameResumedPayload) event.payload();
                System.out.println("Game resumed by: " + resumed.resumedBy());
                refreshGameState();
            }
            case GameEvent.GAME_ENDED -> {
                GameEndedPayload ended = (GameEndedPayload) event.payload();
                System.out.println("Game ended! Reason: " + ended.reason() + ", Winner: " + ended.winner());
                gameLoopRunning = false;
                // Refresh to get final state, then handle end
                try {
                    GameResponse finalGame = apiController.fetchGameStatus(currentGame.id());
                    this.currentGame = finalGame;
                    Platform.runLater(() -> handleGameEnd(finalGame));
                } catch (Exception e) {
                    Platform.runLater(() -> handleGameEndFromEvent(ended));
                }
            }
            case GameEvent.NEGOTIATION_ERROR -> {
                NegotiationErrorPayload error = (NegotiationErrorPayload) event.payload();
                Platform.runLater(() -> 
                    AlertUtils.showAlert("Negotiation Error", error.message())
                );
            }
        }
    }
    
    /**
     * Handle game end from WebSocket event when REST fails.
     */
    private void handleGameEndFromEvent(GameEndedPayload ended) {
        gameLoopRunning = false;
        setNegotiationMode(false);
        isMyTurn = false;
        statusLabel.setText("Game Over: " + ended.reason());
        statusLabel.setTextFill(Color.BLACK);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Winner: " + ended.winner());
        
        if (ended.score() != null) {
            var score = ended.score();
            alert.setContentText(String.format(
                "Black Total: %.1f\nWhite Total: %.1f\nScore Difference: %.1f",
                score.blackTotal(), score.whiteTotal(), score.scoreDifference()
            ));
        } else if (ended.resignedBy() != null) {
            alert.setContentText("Resigned by: " + ended.resignedBy());
        }
        
        alert.showAndWait();
        returnToMenu();
    }

    private void refreshGameState() {
        try {
            
            GameResponse game = apiController.fetchGameStatus(currentGame.id());
            this.currentGame = game;
            
            BoardResponseDTO board = apiController.fetchBoard(currentGame.id());

            String status = game.status();
            if ("NEGOTIATING".equals(status)) {
                this.isNegotiating = true;
                this.currentNegotiation = apiController.getNegotiationState(game.id(), myPlayerId);
            } else {
                this.isNegotiating = false;
                if (!"FINISHED".equals(status) && !"RESIGNED".equals(status)) {
                    this.currentNegotiation = null;
                }
            }

            Platform.runLater(() -> {
                updateUI(game, board);
            });
            
        } catch (Exception e) {
            System.err.println("Błąd odświeżania stanu gry: " + e.getMessage());
        }
    }

    private void updateUI(GameResponse game, BoardResponseDTO board) {
        // Update turn status
        boolean itIsMyTurn = myColor.equals(game.currentTurn());
        this.isMyTurn = itIsMyTurn;
        
        if (isNegotiating) {
            statusLabel.setText("NEGOTIATION PHASE");
            statusLabel.setTextFill(Color.BLACK);
            
            setNegotiationMode(true);
            
            if (currentNegotiation != null) {
                boolean acceptedByMe = "BLACK".equals(myColor) ? currentNegotiation.blackAccepted() : currentNegotiation.whiteAccepted();
                
                if (acceptedByMe) {
                    acceptButton.setDisable(true);
                    acceptButton.setText("ACCEPTED");
                } else {
                    acceptButton.setDisable(false);
                    acceptButton.setText("ACCEPT SCORE");
                }
                
                // Show score preview if available
                if (currentNegotiation.scorePreview() != null) {
                    blackTerritoryLabel.setText("Territory: " + currentNegotiation.scorePreview().blackTerritory());
                    whiteTerritoryLabel.setText("Territory: " + currentNegotiation.scorePreview().whiteTerritory());
                }
            }
            
        } else {
            // Normal game state
            setNegotiationMode(false);
        
            if (itIsMyTurn) {
                statusLabel.setText("YOUR TURN (" + myColor + ")");
                statusLabel.setTextFill(Color.GREEN);
            } else {
                statusLabel.setText("Opponent's turn...");
                statusLabel.setTextFill(Color.RED);
            }
        
            // Update captured stones and territory existing logic
            if (game.blackPlayer() != null) {
                blackCapturedLabel.setText("Captured: " + game.blackPlayer().capturedStones());
                blackTerritoryLabel.setText("Territory: " + board.blackTerritory());
            }
            if (game.whitePlayer() != null) {
                whiteCapturedLabel.setText("Captured: " + game.whitePlayer().capturedStones());
                whiteTerritoryLabel.setText("Territory: " + board.whiteTerritory());
            }
        }

        // Redraw stones
        clearStones();
        
        Set<String> deadPositions = new HashSet<>();
        if (isNegotiating && currentNegotiation != null && currentNegotiation.chains() != null) {
            for (NegotiationStateResponse.ChainInfoDto chain : currentNegotiation.chains()) {
                if ("DEAD".equals(chain.status())) {
                    for (NegotiationStateResponse.PositionDto pos : chain.positions()) {
                        deadPositions.add(pos.x() + "," + pos.y());
                    }
                }
            }
        }
        
        for (StoneDTO stone : board.stones()) {
            boolean isDead = deadPositions.contains(stone.x() + "," + stone.y());
            drawStone(stone.x(), stone.y(), stone.color(), isDead);
        }
    }

    private void clearStones() {
        gamePane.getChildren().removeIf(node -> "stone".equals(node.getUserData()));
    }

    private void drawStone(int x, int y, String color, boolean isDead) {
        Circle stone = new Circle(cellSize / 2.2);
        stone.setUserData("stone");
        stone.setCenterX(BOARD_PADDING + x * cellSize);
        stone.setCenterY(BOARD_PADDING + y * cellSize);
        stone.setMouseTransparent(true);
        
        if (isDead) {
            if ("BLACK".equals(color)) {
                stone.setFill(Color.rgb(0, 0, 0, 0.5)); // Semi-transparent black
            } else {
                stone.setFill(Color.rgb(255, 255, 255, 0.5)); // Semi-transparent white
            }
            stone.setStroke(Color.RED);
            stone.setStrokeWidth(3.0);
            stone.setOpacity(1.0);
        } else {
            if ("BLACK".equals(color)) {
                stone.setFill(Color.BLACK);
                stone.setStroke(Color.WHITE); 
                stone.setStrokeWidth(1.0);
            } else {
                stone.setFill(Color.WHITE);
                stone.setStroke(Color.BLACK);
                stone.setStrokeWidth(1.0);
            }
            stone.setOpacity(1.0);
        }
        
        gamePane.getChildren().add(stone);
    }
    
    private void handleGameEnd(GameResponse game) {
        setNegotiationMode(false);
        isMyTurn = false;
        statusLabel.setText("Game Over: " + game.status());
        statusLabel.setTextFill(Color.BLACK);

        if ("FINISHED".equals(game.status())) {
            new Thread(() -> {
                try {
                    ScoreResponse score = null;
                    
                    // Try to use cached negotiation state first
                    if (currentNegotiation != null && currentNegotiation.scorePreview() != null) {
                        var sp = currentNegotiation.scorePreview();
                        score = new ScoreResponse(
                            sp.blackTerritory(), sp.whiteTerritory(), sp.blackPrisoners(), sp.whitePrisoners(),
                            sp.blackDeadStones(), sp.whiteDeadStones(), sp.komi(), sp.blackTotal(), sp.whiteTotal(),
                            sp.winner(), sp.scoreDifference(), "Game Over"
                        );
                    } else {
                        // Fallback to fetching
                        score = apiController.getScorePreview(game.id(), myPlayerId);
                    }
                    
                    final ScoreResponse finalScore = score;
                    Platform.runLater(() -> {
                        String winner = finalScore.winner();
                        if (winner == null) {
                            winner = finalScore.blackTotal() > finalScore.whiteTotal() ? "BLACK" : "WHITE";
                        }
                        
                        double diff = Math.abs(finalScore.blackTotal() - finalScore.whiteTotal());
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Game Over");
                        alert.setHeaderText("Winner: " + winner);
                        alert.setContentText(String.format(
                            "Black Total: %.1f\n - Territory: %d\n - Prisoners: %d\n - Dead Stones: %d\n\n" +
                            "White Total: %.1f\n - Territory: %d\n - Prisoners: %d\n - Dead Stones: %d\n - Komi: %.1f\n\n" +
                            "Score Difference: %.1f",
                            finalScore.blackTotal(), finalScore.blackTerritory(), finalScore.blackPrisoners(), finalScore.blackDeadStones(),
                            finalScore.whiteTotal(), finalScore.whiteTerritory(), finalScore.whitePrisoners(), finalScore.whiteDeadStones(), finalScore.komi(),
                            diff
                        ));
                        alert.showAndWait();
                        returnToMenu();
                    });
                } catch (Exception e) {
                   e.printStackTrace();
                   Platform.runLater(() -> {
                       AlertUtils.showAlert("Game Over", "Could not fetch final score. Status: " + game.status() + "\nError: " + e.getMessage());
                       returnToMenu();
                   });
                }
            }).start();
        } else if ("RESIGNED".equals(game.status())) {
             String msg = game.message();
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("Game Over");
             alert.setHeaderText("Result: Resignation");
             alert.setContentText(msg != null && !msg.isBlank() ? msg : "A player has resigned.");
             alert.showAndWait();
             returnToMenu();
        } else {
             String msg = game.message();
             if (msg == null || msg.isBlank()) {
                 msg = "Unknown result.";
             }
             
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("Game Over");
             alert.setHeaderText("Game Ended: " + game.status());
             alert.setContentText(msg);
             alert.showAndWait();
             returnToMenu();
        }
    }
    
    private void returnToMenu() {
        if (onGameEnded != null) {
            onGameEnded.run();
        }
    }

    @FXML
    private void onPassClicked() {
        if (!isMyTurn) return;
        new Thread(() -> {
            try {
                apiController.pass(currentGame.id(), myPlayerId);
                refreshGameState();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onResignClicked() {
        new Thread(() -> {
            try {
                apiController.resign(currentGame.id(), myPlayerId);
                // Game loop will catch status change
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @FXML
    private void onAcceptClicked() {
        new Thread(() -> {
            try {
                apiController.acceptScore(currentGame.id(), myPlayerId);
                Platform.runLater(this::refreshGameState);
            } catch (Exception e) {
                Platform.runLater(() -> AlertUtils.showAlert("Error", "Could not accept score: " + e.getMessage()));
            }
        }).start();
    }
    
    @FXML
    private void onResumeClicked() {
        new Thread(() -> {
            try {
                apiController.resumePlaying(currentGame.id(), myPlayerId);
                Platform.runLater(this::refreshGameState);
            } catch (Exception e) {
               Platform.runLater(() -> AlertUtils.showAlert("Error", "Could not resume game: " + e.getMessage()));
            }
        }).start();
    }

    private void setNegotiationMode(boolean isNegotiating) {
        passButton.setVisible(!isNegotiating);
        passButton.setManaged(!isNegotiating);
        resignButton.setVisible(!isNegotiating);
        resignButton.setManaged(!isNegotiating);
        
        resumeButton.setVisible(isNegotiating);
        resumeButton.setManaged(isNegotiating);
        acceptButton.setVisible(isNegotiating);
        acceptButton.setManaged(isNegotiating);
        
        // Hide review button during negotiation
        if (reviewButton != null) {
            reviewButton.setVisible(!isNegotiating);
            reviewButton.setManaged(!isNegotiating);
        }
    }
    
    // ==================== MOVE REVIEW MODE ====================
    
    @FXML
    private void onReviewClicked() {
        if (isInReviewMode) {
            returnToLiveMode();
        } else {
            enterReviewMode();
        }
    }
    
    private void enterReviewMode() {
        // Fetch current move history
        new Thread(() -> {
            try {
                MovesListDTO movesData = apiController.getMovesForGame(currentGame.id());
                
                Platform.runLater(() -> {
                    if (movesData.moves() != null) {
                        moveHistory.clear();
                        moveHistory.addAll(movesData.moves());
                    }
                    
                    if (moveHistory.isEmpty()) {
                        AlertUtils.showAlert("Info", "No moves to review yet.");
                        return;
                    }
                    
                    isInReviewMode = true;
                    reviewMoveIndex = moveHistory.size() - 1; // Start at last move
                    
                    showReviewControls(true);
                    updateReviewBoard();
                    updateReviewControls();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> AlertUtils.showAlert("Error", "Could not load moves: " + e.getMessage()));
            }
        }).start();
    }
    
    private void returnToLiveMode() {
        isInReviewMode = false;
        reviewMoveIndex = -1;
        
        showReviewControls(false);
        
        // Refresh to show live state
        refreshGameState();
    }
    
    private void showReviewControls(boolean show) {
        if (navigationPanel != null) {
            navigationPanel.setVisible(show);
            navigationPanel.setManaged(show);
        }
        if (reviewWarningLabel != null) {
            reviewWarningLabel.setVisible(show);
            reviewWarningLabel.setManaged(show);
        }
        if (reviewButton != null) {
            reviewButton.setText(show ? "🔴" : "📜");
        }
    }
    
    private void updateReviewControls() {
        int totalMoves = moveHistory.size();
        
        if (moveCountLabel != null) {
            moveCountLabel.setText("Move: " + (reviewMoveIndex + 1) + "/" + totalMoves);
        }
        
        if (firstMoveButton != null) firstMoveButton.setDisable(reviewMoveIndex <= 0);
        if (prevMoveButton != null) prevMoveButton.setDisable(reviewMoveIndex <= 0);
        if (nextMoveButton != null) nextMoveButton.setDisable(reviewMoveIndex >= totalMoves - 1);
        if (lastMoveButton != null) lastMoveButton.setDisable(reviewMoveIndex >= totalMoves - 1);
    }
    
    private void updateReviewBoard() {
        // Reconstruct board at current review position
        reconstructReviewBoard(reviewMoveIndex);
        drawReviewBoard();
        updateReviewControls();
    }
    
    private void reconstructReviewBoard(int targetMoveIndex) {
        // Clear board
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                reviewBoardState[x][y] = null;
            }
        }
        
        if (targetMoveIndex < 0 || moveHistory.isEmpty()) {
            return;
        }
        
        // Replay moves up to target index
        for (int i = 0; i <= targetMoveIndex && i < moveHistory.size(); i++) {
            MovesListDTO.MoveInfo move = moveHistory.get(i);
            if (move.x() != null && move.y() != null) {
                reviewBoardState[move.x()][move.y()] = move.color();
            }
        }
        
        // Remove captured stones (simplified - check for no liberties)
        removeDeadStonesFromReview();
    }
    
    private void removeDeadStonesFromReview() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int x = 0; x < boardSize; x++) {
                for (int y = 0; y < boardSize; y++) {
                    if (reviewBoardState[x][y] != null) {
                        if (!hasLibertiesReview(x, y, reviewBoardState[x][y], new boolean[boardSize][boardSize])) {
                            removeGroupReview(x, y, reviewBoardState[x][y]);
                            changed = true;
                        }
                    }
                }
            }
        }
    }
    
    private boolean hasLibertiesReview(int x, int y, String color, boolean[][] visited) {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return false;
        if (visited[x][y]) return false;
        if (reviewBoardState[x][y] == null) return true; // Empty = liberty
        if (!reviewBoardState[x][y].equals(color)) return false;
        
        visited[x][y] = true;
        
        return hasLibertiesReview(x+1, y, color, visited) ||
               hasLibertiesReview(x-1, y, color, visited) ||
               hasLibertiesReview(x, y+1, color, visited) ||
               hasLibertiesReview(x, y-1, color, visited);
    }
    
    private void removeGroupReview(int x, int y, String color) {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return;
        if (reviewBoardState[x][y] == null || !reviewBoardState[x][y].equals(color)) return;
        
        reviewBoardState[x][y] = null;
        removeGroupReview(x+1, y, color);
        removeGroupReview(x-1, y, color);
        removeGroupReview(x, y+1, color);
        removeGroupReview(x, y-1, color);
    }
    
    private void drawReviewBoard() {
        clearStones();
        
        // Draw stones from review state
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (reviewBoardState[x][y] != null) {
                    drawStone(x, y, reviewBoardState[x][y], false);
                }
            }
        }
        
        // Highlight last move
        if (reviewMoveIndex >= 0 && reviewMoveIndex < moveHistory.size()) {
            MovesListDTO.MoveInfo move = moveHistory.get(reviewMoveIndex);
            if (move.x() != null && move.y() != null) {
                // Draw highlight marker
                Circle marker = new Circle(cellSize / 5);
                marker.setUserData("stone");
                marker.setCenterX(BOARD_PADDING + move.x() * cellSize);
                marker.setCenterY(BOARD_PADDING + move.y() * cellSize);
                marker.setFill("BLACK".equals(move.color()) ? Color.WHITE : Color.BLACK);
                marker.setMouseTransparent(true);
                gamePane.getChildren().add(marker);
            }
        }
    }
    
    @FXML
    private void onFirstMoveClicked() {
        if (!isInReviewMode) return;
        reviewMoveIndex = 0;
        updateReviewBoard();
    }
    
    @FXML
    private void onPrevMoveClicked() {
        if (!isInReviewMode || reviewMoveIndex <= 0) return;
        reviewMoveIndex--;
        updateReviewBoard();
    }
    
    @FXML
    private void onNextMoveClicked() {
        if (!isInReviewMode || reviewMoveIndex >= moveHistory.size() - 1) return;
        reviewMoveIndex++;
        updateReviewBoard();
    }
    
    @FXML
    private void onLastMoveClicked() {
        if (!isInReviewMode) return;
        reviewMoveIndex = moveHistory.size() - 1;
        updateReviewBoard();
    }
    
    @FXML
    private void onReturnToLiveClicked() {
        returnToLiveMode();
    }
}
