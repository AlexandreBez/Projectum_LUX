package projectum_lux.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class UserMessageHandler {

	   public void userHandlerModal(String title, String headerText, String message, AlertType type) {
		   switch (type) {
		case ERROR: {
	        Alert alert = new Alert(AlertType.ERROR);
	        alert.setTitle(title);
	        alert.setHeaderText(headerText);
	        alert.setContentText(message);
	        alert.showAndWait();
		}
		case WARNING: {
	        Alert alert = new Alert(AlertType.WARNING);
	        alert.setTitle(title);
	        alert.setHeaderText(headerText);
	        alert.setContentText(message);
	        alert.showAndWait();
		}
		case INFORMATION: {
	        Alert alert = new Alert(AlertType.INFORMATION);
	        alert.setTitle(title);
	        alert.setHeaderText(headerText);
	        alert.setContentText(message);
	        alert.showAndWait();
		}
		case CONFIRMATION: {
	        Alert alert = new Alert(AlertType.CONFIRMATION);
	        alert.setTitle(title);
	        alert.setHeaderText(headerText);
	        alert.setContentText(message);
	        alert.showAndWait();
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}

	 }
}
