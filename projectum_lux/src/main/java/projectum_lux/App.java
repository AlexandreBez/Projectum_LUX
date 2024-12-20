package projectum_lux;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import projectum_lux.controllers.MainAppController;
import projectum_lux.data_control.OV5647Data;
import projectum_lux.data_control.ThermalData;
import projectum_lux.helper.UserDirectorySelectionHandler;
import projectum_lux.helper.UserMessageHandler;
import projectum_lux.raspberry.OV5647Connection;
import projectum_lux.raspberry.PeripheralsConnection;

public class App extends Application{

	private Context pi4j;
	public I2C i2c;
	
    private PeripheralsConnection peripheralsConn;
    private OV5647Connection ov5647Conn;
    
    private ThermalData thermalData;
    private OV5647Data ov5647Data;
    
	private UserMessageHandler userMessageHandler;

	@Override
	public void start(Stage primaryStage) {
	    try {
	        pi4j = Pi4J.newAutoContext();
	        peripheralsConn = new PeripheralsConnection();
	        ov5647Conn = new OV5647Connection();
	        thermalData = new ThermalData();
	        ov5647Data = new OV5647Data();
	        userMessageHandler = new UserMessageHandler();
	        UserDirectorySelectionHandler directoryHandler = new UserDirectorySelectionHandler();

	        i2c = peripheralsConn.setupI2C("amg8833", 1, 0x68, pi4j);
	        ov5647Conn.OV5647connect(0);

	        // Carrega o FXML e passa as dependências ao Controller
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainApp.fxml"));
	        loader.setControllerFactory(param -> {
	            return new MainAppController(
	                peripheralsConn, ov5647Conn, thermalData, ov5647Data, i2c, directoryHandler
	            );
	        });

	        Parent root = loader.load();
	        Scene scene = new Scene(root);

	        // Configuração da Janela
	        primaryStage.setTitle("Vita Scientia | Genesis Scientia - Projectum LUX");
	        primaryStage.setScene(scene);
	        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
	        primaryStage.setMaximized(true);
	        primaryStage.setResizable(true);
	        primaryStage.show();
	    } catch (Exception e) {
	        userMessageHandler.userHandlerModal(
	            "JavaFX app start error",
	            e.getCause() != null ? e.getCause().toString() : "Unknown cause",
	            e.getMessage(),
	            AlertType.ERROR
	        );
	    }
	}


    @Override
    public void stop() throws Exception {
    	peripheralsConn.turnOffPeripheralsConn(pi4j);
    	ov5647Conn.turnOffOV5647Connection();
    	thermalData.stopThermalCamera();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
