package student.pwr.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Utility class for displaying JavaFX alert dialogs.
 * <p>
 * Provides convenient methods for showing information,
 * error, and confirmation dialogs to the user.
 * </p>
 * 
 * @author Go Game Team - PWR
 * @version 1.0
 */
public class AlertUtils {
    
    /**
     * Displays an information alert dialog.
     * <p>
     * Blocks until the user dismisses the dialog.
     * </p>
     * 
     * @param title the dialog window title
     * @param content the message to display
     */
    public static void showAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
