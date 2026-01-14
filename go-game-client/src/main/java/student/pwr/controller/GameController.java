package student.pwr.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
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
import student.pwr.utils.AlertUtils;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import student.pwr.dto.ScoreResponse;
import javafx.scene.control.Alert;

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
    
    private APIController apiController;
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

    @FXML
    public void initialize() {
        System.out.println("Kontroler gry zainicjalizowany");
    }

    public void initGame(GameResponse game, APIController api, UUID playerId) {
        this.apiController = api;
        this.currentGame = game;
        this.myPlayerId = playerId;
        
        if (game.blackPlayer() != null && game.blackPlayer().id().equals(playerId)) {
            this.myColor = "BLACK";
        } else {
            this.myColor = "WHITE";
        }

        // Print URLs for debugging
        String baseUrl = api.getServerURL();
        System.out.println("--- DEBUG LINKS ---");
        System.out.println("Game Status: " + baseUrl + "/api/games/" + game.id());
        System.out.println("Board Status: " + baseUrl + "/api/games/" + game.id() + "/board");
        System.out.println("-------------------");

        this.boardSize = game.boardSize();
        drawBoardGrid();
        
        // Start game loop
        startGameLoop();
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
                         Platform.runLater(() -> {
                             // Force refresh logic
                             refreshGameState();
                         });
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
                    // Refresh game state immediately
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

    private void startGameLoop() {
        Task<Void> gameLoopTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    // Stop loop if game is finished (not IN_PROGRESS and not NEGOTIATION)
                    String status = currentGame.status();
                    if (currentGame != null 
                            && ("RESIGNED".equals(status) || "FINISHED".equals(status))) {
                        Platform.runLater(() -> handleGameEnd(currentGame));
                        break;
                    }

                    refreshGameState();
                    Thread.sleep(1000); 
                }
                return null;
            }
        };

        Thread thread = new Thread(gameLoopTask);
        thread.setDaemon(true);
        thread.start();
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
            statusLabel.setTextFill(Color.ORANGE);
            
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
                    });
                } catch (Exception e) {
                   e.printStackTrace();
                   Platform.runLater(() -> AlertUtils.showAlert("Game Over", "Could not fetch final score. Status: " + game.status() + "\nError: " + e.getMessage()));
                }
            }).start();
        } else if ("RESIGNED".equals(game.status())) {
             String msg = game.message();
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("Game Over");
             alert.setHeaderText("Result: Resignation");
             alert.setContentText(msg != null && !msg.isBlank() ? msg : "A player has resigned.");
             alert.showAndWait();
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
    }
}
