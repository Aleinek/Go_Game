package student.pwr;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ładowanie FXML
        Parent root = FXMLLoader.load(getClass().getResource("/student/pwr/Main.fxml"));
        
        Scene scene = new Scene(root);
        
        // Ładowanie CSS
        scene.getStylesheets().add(getClass().getResource("/student/pwr/Style.css").toExternalForm());
        
        primaryStage.setTitle("Go Game - PWR Edition");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Metoda dla przycisku w FXML
    public void handleStartGame() {
        System.out.println("Slay! Gra się uruchamia...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}