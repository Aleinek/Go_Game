package student.pwr.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import student.pwr.dto.GameResponse;
import student.pwr.websocket.GameWebSocketClient;

import java.util.UUID;

/**
 * Main FXML controller managing view transitions.
 * <p>
 * Coordinates between:
 * <ul>
 *   <li>{@link LoginController} - login and matchmaking view</li>
 *   <li>{@link GameController} - game board view</li>
 *   <li>{@link HistoryController} - game history view</li>
 *   <li>{@link ReplayController} - game replay view</li>
 * </ul>
 * </p>
 * <p>
 * Handles view switching when a game starts and cleanup on shutdown.
 * </p>
 * 
 * @author Go Game Team - PWR
 * @version 1.0
 */
public class UIController {

    private final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    private APIController apiController;

    @FXML private Pane rootPane;

    @FXML private LoginController loginViewController;
    @FXML private GameController gameViewController;
    @FXML private HistoryController historyViewController;
    @FXML private ReplayController replayViewController;
    
    // Nested views
    @FXML private VBox loginView;
    @FXML private VBox gameView;
    @FXML private VBox historyView;
    @FXML private VBox replayView;

    @FXML
    public void initialize() {
        System.out.println("Główny UIController zainicjalizowany");
        apiController = new APIController(SERVER_URL);
        
        // Setup LoginController
        if (loginViewController != null) {
            loginViewController.setOnGameStarted(this::onGameStarted);
            loginViewController.setOnHistoryRequested(this::onHistoryRequested);
        } else {
            System.err.println("LoginController nie wstrzyknięty!");
        }
        
        // Setup HistoryController
        if (historyViewController != null) {
            historyViewController.setOnBackToMenu(v -> showLoginView());
            historyViewController.setOnReplayRequested(this::onReplayRequested);
        }
        
        // Setup ReplayController
        if (replayViewController != null) {
            replayViewController.setOnBackToHistory(v -> showHistoryView());
        }

        // Show login by default
        showLoginView();
    }
    
    private void showLoginView() {
        loginView.setVisible(true);
        gameView.setVisible(false);
        historyView.setVisible(false);
        replayView.setVisible(false);
        
        // Reset login controller state (button text, etc.)
        if (loginViewController != null) {
            loginViewController.resetState();
        }
    }
    
    private void showGameView() {
        loginView.setVisible(false);
        gameView.setVisible(true);
        historyView.setVisible(false);
        replayView.setVisible(false);
    }
    
    private void showHistoryView() {
        loginView.setVisible(false);
        gameView.setVisible(false);
        historyView.setVisible(true);
        replayView.setVisible(false);
        
        // Load history data
        if (historyViewController != null) {
            historyViewController.loadHistory();
        }
    }
    
    private void showReplayView() {
        loginView.setVisible(false);
        gameView.setVisible(false);
        historyView.setVisible(false);
        replayView.setVisible(true);
    }

    private void onGameStarted(GameResponse game, GameWebSocketClient webSocketClient) {
        System.out.println("Przełączanie do widoku gry...");
        showGameView();

        java.util.UUID playerId = loginViewController.getPlayerId();
        String myColor = loginViewController.getMyColor();

        if (gameViewController != null) {
            gameViewController.setOnGameEnded(this::showLoginView);
            gameViewController.initGame(game, apiController, playerId, webSocketClient, myColor);
        }
    }
    
    private void onHistoryRequested() {
        System.out.println("Przełączanie do widoku historii...");
        showHistoryView();
    }
    
    private void onReplayRequested(UUID gameId) {
        System.out.println("Przełączanie do widoku replay dla gry: " + gameId);
        showReplayView();
        
        if (replayViewController != null) {
            replayViewController.loadReplay(gameId);
        }
    }
    
    /**
     * Wywoływane przy zamykaniu aplikacji - czyści zasoby.
     */
    public void shutdown() {
        if (loginViewController != null) {
            GameWebSocketClient wsClient = loginViewController.getWebSocketClient();
            if (wsClient != null) {
                wsClient.disconnect();
                System.out.println("WebSocket rozłączony");
            }
        }
    }
}
