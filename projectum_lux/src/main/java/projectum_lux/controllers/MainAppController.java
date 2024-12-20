package projectum_lux.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import projectum_lux.App;
import projectum_lux.data_control.OV5647Data;
import projectum_lux.data_control.ThermalData;
import projectum_lux.helper.UserDirectorySelectionHandler;
import projectum_lux.raspberry.OV5647Connection;
import projectum_lux.raspberry.PeripheralsConnection;


public class MainAppController {
	
    private final PeripheralsConnection peripheralsConnection;
    private final OV5647Connection ov5647Connection;
    private final ThermalData thermalData;
    private final OV5647Data ov5647Data;
    private final UserDirectorySelectionHandler userDirectorySelectionHandler;

    private final I2C i2c;
	

    @FXML
    private Button RecordThermalCameraBtn;
    
    @FXML
    private Button StopThermalCameraBtn;
    
    
    //------------------------------
    
    @FXML
    private Button RecordOV5647CameraBtn;
    
    @FXML
    private Button StopOV5647CameraBtn;

    //------------------------------
    
    @FXML
    private Button SystemInfo;
    
    //------------------------------
    
    @FXML
    public Canvas ThermalCameraView;
    
    @FXML
    public Canvas OV5647CameraView;
    
    // ---------------------------------------
    
    public MainAppController(
            PeripheralsConnection peripheralsConn,
            OV5647Connection ov5647Conn,
            ThermalData thermalData,
            OV5647Data ov5647Data,
            I2C i2c,
            UserDirectorySelectionHandler userDirectorySelectionHandler
        ) {
            this.peripheralsConnection = peripheralsConn;
            this.ov5647Connection = ov5647Conn;
            this.thermalData = thermalData;
            this.ov5647Data = ov5647Data;
            this.i2c = i2c;
            this.userDirectorySelectionHandler = userDirectorySelectionHandler;
        }
	
    public void initialize() {
        ov5647Data.updateCanvasWithOV5647Data(OV5647CameraView, ov5647Connection.grabber);
        thermalData.updateCanvasWithThermalData(i2c, OV5647CameraView);
    }

	@FXML
    private void onStartThermalCameraRecord() throws IOException {
    	Optional<File> directory = userDirectorySelectionHandler.videoDirectoryChooserHandler(OV5647CameraView.getScene().getWindow());
    	thermalData.startThermalRecording(directory);
    }

    @FXML
    private void onStopThermalCameraRecord() {
    	thermalData.stopThermalRecording();
    }
    
    // ----------------------------------------
    
    @FXML
    private void onStartOV5647Record() {
    	Optional<File> directory = userDirectorySelectionHandler.videoDirectoryChooserHandler(OV5647CameraView.getScene().getWindow());
    	ov5647Data.startOV5647Record(ov5647Connection.grabber, directory);
    }
    
    @FXML
    private void onStopOV5647Record() {
    	ov5647Data.stopOV5647CameraRecord();
    }
    
    // ----------------------------------------
    
    @FXML
    private void onSystemInfoCheck() {
        String message = String.format(
            "- Thermal Camera recording: %s\n" +
            "- OV5647 Camera recording: %s\n",
            thermalData.THERMAL_IS_RECORDING ? "Yes" : "No",
            ov5647Data.OV5647_is_recording ? "Yes" : "No"
        );

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("System Informations");
        alert.setHeaderText("System status:");
        alert.setContentText(message);

        alert.showAndWait();
    }


}
