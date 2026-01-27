package student.pwr.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import student.pwr.dto.GameHistoryDTO;
import student.pwr.dto.GameHistoryDTO.GameSummary;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Controller for the game history view.
 * Displays list of finished games and allows viewing replays.
 */
public class HistoryController {

    private final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    private APIController apiController;
    
    private Consumer<Void> onBackToMenu;
    private Consumer<UUID> onReplayRequested;

    @FXML private TableView<GameSummary> historyTable;
    @FXML private TableColumn<GameSummary, String> dateColumn;
    @FXML private TableColumn<GameSummary, String> blackPlayerColumn;
    @FXML private TableColumn<GameSummary, String> whitePlayerColumn;
    @FXML private TableColumn<GameSummary, String> resultColumn;
    @FXML private TableColumn<GameSummary, String> sizeColumn;
    @FXML private TableColumn<GameSummary, String> movesColumn;
    @FXML private TableColumn<GameSummary, Void> actionColumn;
    
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private ObservableList<GameSummary> gamesList = FXCollections.observableArrayList();
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    @FXML
    public void initialize() {
        apiController = new APIController(SERVER_URL);
        setupTableColumns();
        historyTable.setItems(gamesList);
    }
    
    public void setOnBackToMenu(Consumer<Void> callback) {
        this.onBackToMenu = callback;
    }
    
    public void setOnReplayRequested(Consumer<UUID> callback) {
        this.onReplayRequested = callback;
    }
    
    public void loadHistory() {
        statusLabel.setText("Loading games...");
        refreshButton.setDisable(true);
        
        new Thread(() -> {
            try {
                GameHistoryDTO history = apiController.getGameHistory();
                
                Platform.runLater(() -> {
                    gamesList.clear();
                    if (history.games() != null) {
                        gamesList.addAll(history.games());
                    }
                    statusLabel.setText("Found " + history.totalCount() + " games");
                    refreshButton.setDisable(false);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error loading history: " + e.getMessage());
                    refreshButton.setDisable(false);
                });
            }
        }).start();
    }

    private void setupTableColumns() {
        // Date column
        dateColumn.setCellValueFactory(cellData -> {
            GameSummary game = cellData.getValue();
            String date = game.finishedAt() != null 
                ? dateFormatter.format(game.finishedAt())
                : (game.createdAt() != null ? dateFormatter.format(game.createdAt()) : "N/A");
            return new SimpleStringProperty(date);
        });
        
        // Black player column
        blackPlayerColumn.setCellValueFactory(cellData -> {
            GameSummary game = cellData.getValue();
            String name = game.blackPlayer() != null ? game.blackPlayer().nickname() : "Unknown";
            return new SimpleStringProperty(name);
        });
        
        // White player column
        whitePlayerColumn.setCellValueFactory(cellData -> {
            GameSummary game = cellData.getValue();
            String name = game.whitePlayer() != null ? game.whitePlayer().nickname() : "Unknown";
            return new SimpleStringProperty(name);
        });
        
        // Result column
        resultColumn.setCellValueFactory(cellData -> {
            GameSummary game = cellData.getValue();
            String result = formatResult(game);
            return new SimpleStringProperty(result);
        });
        
        // Size column
        sizeColumn.setCellValueFactory(cellData -> {
            GameSummary game = cellData.getValue();
            return new SimpleStringProperty(game.boardSize() + "x" + game.boardSize());
        });
        
        // Moves column
        movesColumn.setCellValueFactory(cellData -> {
            GameSummary game = cellData.getValue();
            return new SimpleStringProperty(String.valueOf(game.totalMoves()));
        });
        
        // Action column with Replay button
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button replayBtn = new Button("Replay");
            {
                replayBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px;");
                replayBtn.setOnAction(e -> {
                    GameSummary game = getTableView().getItems().get(getIndex());
                    if (onReplayRequested != null) {
                        onReplayRequested.accept(game.gameId());
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : replayBtn);
            }
        });
    }
    
    private String formatResult(GameSummary game) {
        if ("RESIGNED".equals(game.status())) {
            return game.winner() + "+Res";
        }
        
        if (game.winner() == null) {
            return "N/A";
        }
        
        double scoreDiff = 0;
        if (game.blackScore() != null && game.whiteScore() != null) {
            scoreDiff = Math.abs(game.blackScore() - game.whiteScore());
        }
        
        String winnerShort = "BLACK".equals(game.winner()) ? "B" : "W";
        return winnerShort + "+" + String.format("%.1f", scoreDiff);
    }

    @FXML
    private void onRefreshClicked() {
        loadHistory();
    }

    @FXML
    private void onBackClicked() {
        if (onBackToMenu != null) {
            onBackToMenu.accept(null);
        }
    }
}
