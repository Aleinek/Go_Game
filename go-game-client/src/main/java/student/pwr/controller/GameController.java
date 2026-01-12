package student.pwr.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import student.pwr.dto.BoardResponseDTO;
import student.pwr.dto.GameResponse;
import student.pwr.dto.StoneDTO;
import student.pwr.dto.MoveResponse;
import student.pwr.utils.AlertUtils;
import java.util.UUID;

public class GameController {

    @FXML private Pane gameHeader;
    @FXML private Pane gamePane;
    @FXML private Label statusLabel;
    @FXML private Label blackCapturedLabel;
    @FXML private Label blackTerritoryLabel;
    @FXML private Label whiteCapturedLabel;
    @FXML private Label whiteTerritoryLabel;
    
    private APIController apiController;
    private GameResponse currentGame;
    private UUID myPlayerId;
    private String myColor; 
    private boolean isMyTurn = false;
    private int boardSize;
    private double cellSize;
    private final double BOARD_PADDING = 40.0;

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
                    if (currentGame != null && !"IN_PROGRESS".equals(currentGame.status())) {
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
        
        if (itIsMyTurn) {
            statusLabel.setText("YOUR TURN (" + myColor + ")");
            statusLabel.setTextFill(Color.GREEN);
        } else {
            statusLabel.setText("Opponent's turn...");
            statusLabel.setTextFill(Color.RED);
        }

        // Update captured stones and territory
        if (game.blackPlayer() != null) {
            blackCapturedLabel.setText("Captured: " + game.blackPlayer().capturedStones());
            blackTerritoryLabel.setText("Territory: " + board.blackTerritory());
        }
        if (game.whitePlayer() != null) {
            whiteCapturedLabel.setText("Captured: " + game.whitePlayer().capturedStones());
            whiteTerritoryLabel.setText("Territory: " + board.whiteTerritory());
        }

        // Redraw stones
        clearStones();
        
        for (StoneDTO stone : board.stones()) {
            drawStone(stone.x(), stone.y(), stone.color());
        }
    }

    private void clearStones() {
        gamePane.getChildren().removeIf(node -> "stone".equals(node.getUserData()));
    }

    private void drawStone(int x, int y, String color) {
        Circle stone = new Circle(cellSize / 2.2);
        stone.setUserData("stone");
        stone.setCenterX(BOARD_PADDING + x * cellSize);
        stone.setCenterY(BOARD_PADDING + y * cellSize);
        
        if ("BLACK".equals(color)) {
            stone.setFill(Color.BLACK);
            stone.setStroke(Color.WHITE); 
        } else {
            stone.setFill(Color.WHITE);
            stone.setStroke(Color.BLACK);
        }
        
        gamePane.getChildren().add(stone);
    }
    
    private void handleGameEnd(GameResponse game) {
        statusLabel.setText("Game Over: " + game.status());
        isMyTurn = false;
        AlertUtils.showAlert("Koniec gry", "Gra zakończona! Status: " + game.status());
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
}
