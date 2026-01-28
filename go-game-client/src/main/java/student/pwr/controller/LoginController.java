package student.pwr.controller;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

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
import student.pwr.dto.websocket.GameStartedPayload;
import student.pwr.utils.AlertUtils;
import student.pwr.websocket.GameWebSocketClient;

/**
 * FXML controller for the login/matchmaking screen.
 * <p>
 * Handles:
 * <ul>
 *   <li>Player registration with nickname</li>
 *   <li>Board size selection (9, 13, 19)</li>
 *   <li>WebSocket connection establishment</li>
 *   <li>Matchmaking and waiting for opponent</li>
 * </ul>
 * </p>
 * <p>
 * Uses WebSocket events to detect when an opponent joins,
 * rather than polling the server.
 * </p>
 * 
 * @author Go Game Team - PWR
 * @version 1.0
 */
public class LoginController {

    private final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    private APIController apiController;
    private GameWebSocketClient webSocketClient;
    private BiConsumer<GameResponse, GameWebSocketClient> onGameStarted;
    private Runnable onHistoryRequested;
    private UUID playerId;
    private String myColor;

    @FXML private TextField usernameField;
    @FXML private ToggleGroup boardSizeGroup;
    @FXML private RadioButton size19;
    @FXML private RadioButton size13;
    @FXML private RadioButton size9;
    @FXML private Button searchGameButton;
    @FXML private Button playWithBotButton;
    @FXML private Button historyButton;

    @FXML
    public void initialize() {
        apiController = new APIController(SERVER_URL);
        webSocketClient = new GameWebSocketClient(SERVER_URL);
    }

    public void setOnGameStarted(BiConsumer<GameResponse, GameWebSocketClient> onGameStarted) {
        this.onGameStarted = onGameStarted;
    }
    
    public void setOnHistoryRequested(Runnable onHistoryRequested) {
        this.onHistoryRequested = onHistoryRequested;
    }

    public UUID getPlayerId() {
        return playerId;
    }
    
    public String getMyColor() {
        return myColor;
    }
    
    public GameWebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
    
    /**
     * Resets the login view state after a game ends.
     */
    public void resetState() {
        searchGameButton.setDisable(false);
        searchGameButton.setText("Search Game");
        playWithBotButton.setDisable(false);
        playWithBotButton.setText("🤖 PLAY WITH BOT");
        // Create fresh WebSocket client for next game
        webSocketClient = new GameWebSocketClient(SERVER_URL);
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
                
                // Połącz WebSocket PRZED dołączeniem do kolejki
                Platform.runLater(() -> searchGameButton.setText("Connecting WebSocket..."));
                webSocketClient.connect(playerId);
                System.out.println("WebSocket połączony dla gracza: " + playerId);
                
                Platform.runLater(() -> searchGameButton.setText("Searching for game..."));

                System.out.println("Dołączanie do gry, rozmiar: " + finalBoardSize);
                GameResponse game = apiController.joinGame(playerId, finalBoardSize);

                if ("WAITING".equals(game.status())) {
                    System.out.println("Oczekiwanie na przeciwnika przez WebSocket...");
                    waitForOpponentViaWebSocket(game.id());
                } else if ("IN_PROGRESS".equals(game.status()) || "NEGOTIATING".equals(game.status())) {
                    System.out.println("Gra znaleziona od razu! ID: " + game.id());
                    // Ustal kolor gracza
                    if (game.blackPlayer() != null && game.blackPlayer().id().equals(playerId)) {
                        this.myColor = "BLACK";
                    } else {
                        this.myColor = "WHITE";
                    }
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

    /**
     * Czeka na przeciwnika przez WebSocket zamiast pollingu HTTP.
     */
    private void waitForOpponentViaWebSocket(UUID waitingId) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                GameStartedPayload gameStarted = null;
                
                // Czekaj na event GAME_STARTED przez WebSocket
                while (gameStarted == null) {
                    gameStarted = webSocketClient.waitForGameStart(2, TimeUnit.SECONDS);
                    if (gameStarted == null) {
                        System.out.print(".");
                    }
                }
                
                System.out.println("\nGra rozpoczęta! ID: " + gameStarted.gameId());
                myColor = gameStarted.yourColor();
                
                // Pobierz pełny stan gry przez REST
                GameResponse game = apiController.fetchGameStatus(gameStarted.gameId());
                
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
            onGameStarted.accept(game, webSocketClient);
        }
    }
    
    @FXML
    private void onHistoryClicked() {
        if (onHistoryRequested != null) {
            onHistoryRequested.run();
        }
    }
    
    /**
     * Handles click on "PLAY WITH BOT" button.
     * Creates a game against a bot opponent without waiting for matchmaking.
     */
    @FXML
    private void onPlayWithBotClicked() {
        String nickname = usernameField.getText();
        if (nickname == null || nickname.trim().isEmpty()) {
            AlertUtils.showAlert("Error", "Enter username!");
            return;
        }

        int boardSize = 19;
        if (size13.isSelected()) boardSize = 13;
        if (size9.isSelected()) boardSize = 9;

        // Disable both buttons
        searchGameButton.setDisable(true);
        playWithBotButton.setDisable(true);
        playWithBotButton.setText("Starting game...");

        final int finalBoardSize = boardSize;

        new Thread(() -> {
            try {
                System.out.println("Rejestracja gracza: " + nickname);
                PlayerResponse player = apiController.registerPlayer(nickname);
                this.playerId = player.id();
                
                // Connect WebSocket
                Platform.runLater(() -> playWithBotButton.setText("Connecting..."));
                webSocketClient.connect(playerId);
                System.out.println("WebSocket połączony dla gracza: " + playerId);
                
                // Create game with bot (instant start)
                Platform.runLater(() -> playWithBotButton.setText("Creating game with bot..."));
                System.out.println("Tworzenie gry z botem, rozmiar: " + finalBoardSize);
                GameResponse game = apiController.joinGameWithBot(playerId, finalBoardSize);
                
                // Player is always BLACK vs bot
                this.myColor = "BLACK";
                System.out.println("Gra z botem utworzona! ID: " + game.id());
                
                Platform.runLater(() -> notifyGameStarted(game));

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    AlertUtils.showAlert("Error", "Failed to start game with bot: " + e.getMessage());
                    searchGameButton.setDisable(false);
                    playWithBotButton.setDisable(false);
                    playWithBotButton.setText("🤖 PLAY WITH BOT");
                });
            }
        }).start();
    }
}
