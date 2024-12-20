package projectum_lux.helper;

import java.io.File;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class UserDirectorySelectionHandler {
	
	public Optional<File> videoDirectoryChooserHandler(Window window) {
	    DirectoryChooser directoryChooser = new DirectoryChooser();
	    directoryChooser.setTitle("Select a folder to save");

	    File diretorioSelecionado = null;

	    while (diretorioSelecionado == null) { 
	    	
	    	diretorioSelecionado = directoryChooser.showDialog(window);
	        
	        if (diretorioSelecionado == null) {
	            // Exibe uma mensagem de erro e força o usuário a tentar novamente
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Obligatory selection");
	            alert.setHeaderText("Any folder selected!!!");
	            alert.setContentText("Please, select one folder to save the camera video and data.");
	            alert.showAndWait();
	        }
	    }

	    return Optional.of(diretorioSelecionado);
	}
    
}
