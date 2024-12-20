package projectum_lux.raspberry;

import org.bytedeco.javacv.OpenCVFrameGrabber;

import javafx.scene.control.Alert.AlertType;
import projectum_lux.data_control.OV5647Data;
import projectum_lux.helper.UserMessageHandler;

public class OV5647Connection {

	private UserMessageHandler messageHandler;
	private OV5647Data ov5647Data;
	public OpenCVFrameGrabber grabber;

	public void OV5647connect(int deviceIndex) {
		try {
			grabber = new OpenCVFrameGrabber(deviceIndex);
			grabber.start();
		} catch (Exception e) {
			messageHandler.userHandlerModal("OV5647 error", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
		}
	}

	public void turnOffOV5647Connection() {
		try {

			if (ov5647Data.THREAD_OV5647_UPDATE_CANVAS != null) {
				ov5647Data.THREAD_OV5647_UPDATE_CANVAS.interrupt();
			}
			
			grabber.stop();
			grabber.release();

		} catch (Exception e) {
			System.err.println("Erro ao parar a câmera: " + e.getMessage());
		}
	}
}
