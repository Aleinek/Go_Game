package student.pwr;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * JavaFX Application class for the Go Game client.
 * <p>
 * Initializes the JavaFX runtime and loads the main UI from FXML.
 * The application uses:
 * <ul>
 *   <li>FXML for UI layout definition</li>
 *   <li>CSS for styling</li>
 *   <li>Controller classes for business logic</li>
 * </ul>
 * </p>
 * 
 * @author Go Game Team - PWR
 * @version 1.0
 */
public class App extends Application {

    
    /**
     * Initializes and displays the primary stage.
     * <p>
     * Loads the main FXML layout and applies CSS styling.
     * </p>
     * 
     * @param primaryStage the primary stage for this application
     * @throws Exception if FXML or CSS cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ładowanie FXML
        Parent root = FXMLLoader.load(getClass().getResource("/student/pwr/Main.fxml"));
        
        Scene scene = new Scene(root);
        
        // Ładowanie CSS
        scene.getStylesheets().add(getClass().getResource("/student/pwr/Style.css").toExternalForm());
        
        primaryStage.setTitle("Go Game - PWR Edition");
        primaryStage.setResizable(true);
        primaryStage.setMaximized(false);

        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    

    public static void main(String[] args) {
        launch(args);
    }
}