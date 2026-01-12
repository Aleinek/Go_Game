package student.pwr.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import student.pwr.dto.GameResponse;

public class UIController {

    private final String SERVER_URL = "http://gogame.adamkulwicki.pl:8080";
    private APIController apiController;

    @FXML private Pane rootPane;

    @FXML private LoginController loginViewController;
    @FXML private GameController gameViewController;
    
    // Nested views
    @FXML private VBox loginView;
    @FXML private VBox gameView;

    @FXML
    public void initialize() {
        System.out.println("Główny UIController zainicjalizowany");
        apiController = new APIController(SERVER_URL);
        
        // Setup LoginController
        if (loginViewController != null) {
            loginViewController.setOnGameStarted(this::onGameStarted);
        } else {
            System.err.println("LoginController nie wstrzyknięty!");
        }

        // Show login by default
        loginView.setVisible(true);
        gameView.setVisible(false);
    }

    private void onGameStarted(GameResponse game) {
        System.out.println("Przełączanie do widoku gry...");
        
        loginView.setVisible(false);
        gameView.setVisible(true);

        java.util.UUID playerId = loginViewController.getPlayerId();

        if (gameViewController != null) {
            gameViewController.initGame(game, apiController, playerId);
        }
    }
}
