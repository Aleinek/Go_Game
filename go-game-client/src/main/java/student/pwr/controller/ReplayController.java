package student.pwr.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import student.pwr.dto.GameReplayDTO;
import student.pwr.dto.GameReplayDTO.ReplayMove;

import java.util.*;
import java.util.function.Consumer;

/**
 * Controller for game replay view.
 * Allows step-by-step replay of finished games.
 */
public class ReplayController {

    private final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    private APIController apiController;
    
    private Consumer<Void> onBackToHistory;
    
    @FXML private Label titleLabel;
    @FXML private Label blackInfoLabel;
    @FXML private Label whiteInfoLabel;
    @FXML private Label resultLabel;
    @FXML private Label moveCounterLabel;
    @FXML private Label currentMoveLabel;
    
    @FXML private Button firstButton;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Button lastButton;
    @FXML private Button autoPlayButton;
    @FXML private Button backButton;
    
    @FXML private Pane replayPane;

    private GameReplayDTO replayData;
    private int currentMoveIndex = -1; // -1 = empty board
    private int boardSize;
    private double cellSize;
    private final double BOARD_PADDING = 40.0;
    
    // Board state for reconstruction
    private String[][] boardState;
    
    // Auto-play
    private Timeline autoPlayTimeline;
    private boolean isAutoPlaying = false;

    @FXML
    public void initialize() {
        apiController = new APIController(SERVER_URL);
    }
    
    public void setOnBackToHistory(Consumer<Void> callback) {
        this.onBackToHistory = callback;
    }
    
    public void loadReplay(UUID gameId) {
        titleLabel.setText("Loading...");
        
        new Thread(() -> {
            try {
                GameReplayDTO data = apiController.getReplayData(gameId);
                
                Platform.runLater(() -> {
                    this.replayData = data;
                    this.boardSize = data.boardSize();
                    this.boardState = new String[boardSize][boardSize];
                    initializeReplay();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    titleLabel.setText("Error loading replay");
                    currentMoveLabel.setText(e.getMessage());
                });
            }
        }).start();
    }
    
    private void initializeReplay() {
        // Set game info
        String blackName = replayData.blackPlayer() != null ? replayData.blackPlayer().nickname() : "Unknown";
        String whiteName = replayData.whitePlayer() != null ? replayData.whitePlayer().nickname() : "Unknown";
        
        titleLabel.setText("Game Replay - " + boardSize + "x" + boardSize);
        blackInfoLabel.setText("Black: " + blackName);
        whiteInfoLabel.setText("White: " + whiteName);
        
        // Format result
        String result = formatResult();
        resultLabel.setText("Result: " + result);
        
        // Draw empty board
        currentMoveIndex = -1;
        clearBoardState();
        drawBoard();
        updateControls();
    }
    
    private String formatResult() {
        if ("RESIGNED".equals(replayData.finalStatus())) {
            return replayData.winner() + " wins by resignation";
        }
        
        if (replayData.winner() == null) {
            return "Unknown";
        }
        
        double scoreDiff = 0;
        if (replayData.blackScore() != null && replayData.whiteScore() != null) {
            scoreDiff = Math.abs(replayData.blackScore() - replayData.whiteScore());
        }
        
        return replayData.winner() + " wins by " + String.format("%.1f", scoreDiff) + " points";
    }
    
    private void clearBoardState() {
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                boardState[x][y] = null;
            }
        }
    }
    
    private void drawBoard() {
        replayPane.getChildren().clear();
        
        double width = replayPane.getPrefWidth();
        double height = replayPane.getPrefHeight();
        double boardWidth = width - 2 * BOARD_PADDING;
        
        this.cellSize = boardWidth / (boardSize - 1);

        // Background
        Rectangle bg = new Rectangle(width, height, Color.web("#DEB887"));
        replayPane.getChildren().add(bg);

        // Grid lines
        for (int i = 0; i < boardSize; i++) {
            Line vLine = new Line(
                BOARD_PADDING + i * cellSize, BOARD_PADDING, 
                BOARD_PADDING + i * cellSize, BOARD_PADDING + (boardSize - 1) * cellSize
            );
            Line hLine = new Line(
                BOARD_PADDING, BOARD_PADDING + i * cellSize, 
                BOARD_PADDING + (boardSize - 1) * cellSize, BOARD_PADDING + i * cellSize
            );
            vLine.setStroke(Color.BLACK);
            hLine.setStroke(Color.BLACK);
            replayPane.getChildren().addAll(vLine, hLine);
        }

        // Star points (hoshi)
        drawStarPoints();

        // Draw stones from board state
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (boardState[x][y] != null) {
                    drawStone(x, y, boardState[x][y], false);
                }
            }
        }
        
        // Highlight last move
        if (currentMoveIndex >= 0 && replayData.moves() != null && currentMoveIndex < replayData.moves().size()) {
            ReplayMove lastMove = replayData.moves().get(currentMoveIndex);
            if (!lastMove.isPass() && lastMove.x() != null && lastMove.y() != null) {
                drawStone(lastMove.x(), lastMove.y(), lastMove.playerColor(), true);
            }
        }
    }
    
    private void drawStarPoints() {
        int[] starPositions;
        if (boardSize == 19) {
            starPositions = new int[]{3, 9, 15};
        } else if (boardSize == 13) {
            starPositions = new int[]{3, 6, 9};
        } else if (boardSize == 9) {
            starPositions = new int[]{2, 4, 6};
        } else {
            return;
        }
        
        for (int x : starPositions) {
            for (int y : starPositions) {
                Circle star = new Circle(3);
                star.setFill(Color.BLACK);
                star.setCenterX(BOARD_PADDING + x * cellSize);
                star.setCenterY(BOARD_PADDING + y * cellSize);
                replayPane.getChildren().add(star);
            }
        }
    }
    
    private void drawStone(int x, int y, String color, boolean highlight) {
        double radius = cellSize * 0.45;
        Circle stone = new Circle(radius);
        stone.setCenterX(BOARD_PADDING + x * cellSize);
        stone.setCenterY(BOARD_PADDING + y * cellSize);
        
        if ("BLACK".equals(color)) {
            stone.setFill(Color.BLACK);
            stone.setStroke(highlight ? Color.RED : Color.DARKGRAY);
        } else {
            stone.setFill(Color.WHITE);
            stone.setStroke(highlight ? Color.RED : Color.BLACK);
        }
        stone.setStrokeWidth(highlight ? 3 : 1);
        
        replayPane.getChildren().add(stone);
    }
    
    private void reconstructBoardAtMove(int targetMoveIndex) {
        clearBoardState();
        
        if (targetMoveIndex < 0 || replayData.moves() == null) {
            return;
        }
        
        // Simple reconstruction - just place stones
        // Note: This doesn't handle captures properly, would need full game logic
        for (int i = 0; i <= targetMoveIndex && i < replayData.moves().size(); i++) {
            ReplayMove move = replayData.moves().get(i);
            if (!move.isPass() && move.x() != null && move.y() != null) {
                boardState[move.x()][move.y()] = move.playerColor();
            }
        }
        
        // Remove captured stones (simplified - based on captured count in moves)
        // For proper implementation, would need to track captures
        removeDeadStones();
    }
    
    private void removeDeadStones() {
        // Simplified capture detection - check for groups with no liberties
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int x = 0; x < boardSize; x++) {
                for (int y = 0; y < boardSize; y++) {
                    if (boardState[x][y] != null) {
                        if (!hasLiberties(x, y, boardState[x][y], new boolean[boardSize][boardSize])) {
                            // Remove this group
                            removeGroup(x, y, boardState[x][y]);
                            changed = true;
                        }
                    }
                }
            }
        }
    }
    
    private boolean hasLiberties(int x, int y, String color, boolean[][] visited) {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return false;
        if (visited[x][y]) return false;
        if (boardState[x][y] == null) return true; // Empty = liberty
        if (!boardState[x][y].equals(color)) return false;
        
        visited[x][y] = true;
        
        return hasLiberties(x+1, y, color, visited) ||
               hasLiberties(x-1, y, color, visited) ||
               hasLiberties(x, y+1, color, visited) ||
               hasLiberties(x, y-1, color, visited);
    }
    
    private void removeGroup(int x, int y, String color) {
        if (x < 0 || x >= boardSize || y < 0 || y >= boardSize) return;
        if (boardState[x][y] == null || !boardState[x][y].equals(color)) return;
        
        boardState[x][y] = null;
        removeGroup(x+1, y, color);
        removeGroup(x-1, y, color);
        removeGroup(x, y+1, color);
        removeGroup(x, y-1, color);
    }
    
    private void updateControls() {
        int totalMoves = replayData.moves() != null ? replayData.moves().size() : 0;
        
        moveCounterLabel.setText("Move: " + (currentMoveIndex + 1) + " / " + totalMoves);
        
        firstButton.setDisable(currentMoveIndex < 0);
        prevButton.setDisable(currentMoveIndex < 0);
        nextButton.setDisable(currentMoveIndex >= totalMoves - 1);
        lastButton.setDisable(currentMoveIndex >= totalMoves - 1);
        
        // Update current move description
        if (currentMoveIndex < 0) {
            currentMoveLabel.setText("Empty board");
        } else if (replayData.moves() != null && currentMoveIndex < replayData.moves().size()) {
            ReplayMove move = replayData.moves().get(currentMoveIndex);
            if (move.isPass()) {
                currentMoveLabel.setText(move.playerColor() + " passed");
            } else {
                currentMoveLabel.setText(move.playerColor() + " played at (" + move.x() + ", " + move.y() + ")");
            }
        }
    }
    
    private void goToMove(int moveIndex) {
        int totalMoves = replayData.moves() != null ? replayData.moves().size() : 0;
        currentMoveIndex = Math.max(-1, Math.min(moveIndex, totalMoves - 1));
        reconstructBoardAtMove(currentMoveIndex);
        drawBoard();
        updateControls();
    }

    @FXML
    private void onFirstClicked() {
        stopAutoPlay();
        goToMove(-1);
    }

    @FXML
    private void onPrevClicked() {
        stopAutoPlay();
        goToMove(currentMoveIndex - 1);
    }

    @FXML
    private void onNextClicked() {
        stopAutoPlay();
        goToMove(currentMoveIndex + 1);
    }

    @FXML
    private void onLastClicked() {
        stopAutoPlay();
        int totalMoves = replayData.moves() != null ? replayData.moves().size() : 0;
        goToMove(totalMoves - 1);
    }

    @FXML
    private void onAutoPlayClicked() {
        if (isAutoPlaying) {
            stopAutoPlay();
        } else {
            startAutoPlay();
        }
    }
    
    private void startAutoPlay() {
        isAutoPlaying = true;
        autoPlayButton.setText("⏸ Stop");
        autoPlayButton.setStyle("-fx-font-size: 14px; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        autoPlayTimeline = new Timeline(new KeyFrame(Duration.millis(800), e -> {
            int totalMoves = replayData.moves() != null ? replayData.moves().size() : 0;
            if (currentMoveIndex < totalMoves - 1) {
                goToMove(currentMoveIndex + 1);
            } else {
                stopAutoPlay();
            }
        }));
        autoPlayTimeline.setCycleCount(Timeline.INDEFINITE);
        autoPlayTimeline.play();
    }
    
    private void stopAutoPlay() {
        isAutoPlaying = false;
        autoPlayButton.setText("▶ Auto");
        autoPlayButton.setStyle("-fx-font-size: 14px; -fx-background-color: #27ae60; -fx-text-fill: white;");
        
        if (autoPlayTimeline != null) {
            autoPlayTimeline.stop();
            autoPlayTimeline = null;
        }
    }

    @FXML
    private void onBackClicked() {
        stopAutoPlay();
        if (onBackToHistory != null) {
            onBackToHistory.accept(null);
        }
    }
}
