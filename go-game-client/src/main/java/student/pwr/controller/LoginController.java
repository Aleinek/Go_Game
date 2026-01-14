package student.pwr.controller;

import java.util.UUID;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import student.pwr.dto.GameResponse;
import student.pwr.dto.PlayerResponse;
import student.pwr.dto.WaitingStatus;
import student.pwr.utils.AlertUtils;

public class LoginController {

    private final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    private APIController apiController;
    private Consumer<GameResponse> onGameStarted; 
    private UUID playerId;

    @FXML private TextField usernameField;
    @FXML private ToggleGroup boardSizeGroup;
    @FXML private RadioButton size19;
    @FXML private RadioButton size13;
    @FXML private RadioButton size9;
    @FXML private Button searchGameButton;

    @FXML
    public void initialize() {
        apiController = new APIController(SERVER_URL);
    }

    public void setOnGameStarted(Consumer<GameResponse> onGameStarted) {
        this.onGameStarted = onGameStarted;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    @FXML
    private void onSearchGameClicked() {
        String nickname = usernameField.getText();
        if (nickname == null || nickname.trim().isEmpty()) {
            AlertUtils.showAlert("Error", "Enter username!");
            return;
        }

        int boardSize = 19;
        if (size13.isSelected()) boardSize = 13;
        if (size9.isSelected()) boardSize = 9;

        searchGameButton.setDisable(true);
        searchGameButton.setText("Connecting...");

        final int finalBoardSize = boardSize;

        new Thread(() -> {
            try {
                System.out.println("Rejestracja gracza: " + nickname);
                PlayerResponse player = apiController.registerPlayer(nickname);
                this.playerId = player.id();
                
                Platform.runLater(() -> searchGameButton.setText("Searching for game..."));

                System.out.println("Dołączanie do gry, rozmiar: " + finalBoardSize);
                GameResponse game = apiController.joinGame(playerId, finalBoardSize);

                if ("WAITING".equals(game.status())) {
                    System.out.println("Oczekiwanie na przeciwnika...");
                    waitForOpponent(game.id());
                } else if ("IN_PROGRESS".equals(game.status()) || "NEGOTIATING".equals(game.status())) {
                    System.out.println("Gra znaleziona od razu! ID: " + game.id());
                    Platform.runLater(() -> notifyGameStarted(game));
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    AlertUtils.showAlert("Error", "Connection error: " + e.getMessage());
                    searchGameButton.setDisable(false);
                    searchGameButton.setText("Search Game");
                });
            }
        }).start();
    }

    private void waitForOpponent(UUID waitingId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                WaitingStatus status;
                do {
                    Thread.sleep(1000);
                    status = apiController.checkWaitingStatus(waitingId);
                    System.out.print(".");
                } while ("WAITING".equals(status.status()));

                UUID foundGameId = status.gameId();
                GameResponse game = apiController.fetchGameStatus(foundGameId);
                
                Platform.runLater(() -> notifyGameStarted(game));
                return null;
            }
        };

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                AlertUtils.showAlert("Error", "Error waiting for game.");
                searchGameButton.setDisable(false);
                searchGameButton.setText("Search Game");
            });
        });

        new Thread(task).start();
    }

    private void notifyGameStarted(GameResponse game) {
        if (onGameStarted != null) {
            onGameStarted.accept(game);
        }
    }
}
