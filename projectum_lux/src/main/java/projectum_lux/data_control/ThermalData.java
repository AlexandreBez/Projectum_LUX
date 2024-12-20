package projectum_lux.data_control;

import javafx.scene.paint.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import com.pi4j.io.i2c.I2C;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Font;

import projectum_lux.helper.UserMessageHandler;

public class ThermalData {

	private UserMessageHandler messageHandler;
	private ThermalExcelData thermalExcelData;

	private static final int AMG8833_PIXEL_START = 0x80;
	private static final int PIXEL_COUNT = 64;
	private static final double PIXEL_TEMP_MULTIPLIER = 0.25;
	
	private double[][] matrix = new double[8][8];
	private List<double[][]> container;
	private Optional<File> data_directory;
	
	private Thread THREAD_THERMAL_CAMERA;
	public boolean THREAD_THERMAL_RUNNING = false;
    public boolean THERMAL_IS_RECORDING = false;
    
    
    public void updateCanvasWithThermalData(I2C i2c, Canvas canvas) {
    	
        if (THREAD_THERMAL_RUNNING) {
            return;
        }

        THREAD_THERMAL_RUNNING = true;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        THREAD_THERMAL_CAMERA = new Thread(() -> {
            while (THREAD_THERMAL_RUNNING) {
            	
            	byte[] buffer = new byte[PIXEL_COUNT * 2];
            	
            	i2c.readRegister(AMG8833_PIXEL_START, buffer, 0, buffer.length);

                for (int i = 0; i < PIXEL_COUNT; i++) {
                    int tempRaw = (buffer[i * 2 + 1] << 8) | (buffer[i * 2] & 0xFF);
                    if ((tempRaw & 0x800) != 0) {
                        tempRaw |= 0xF000;
                    }
                    matrix[i / 8][i % 8] = tempRaw * PIXEL_TEMP_MULTIPLIER;
                }
                
                if (THERMAL_IS_RECORDING) {
                	String formattedDate = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss").format(new Date());
                	double[][] cloneHelper = new double[matrix.length][matrix[0].length];
                	for (int i = 0; i < matrix.length; i++) {
                	    cloneHelper[i] = matrix[i].clone();
                	}
                    container.add(cloneHelper);
                    
                    thermalExcelData.saveDataOnNewLine(formattedDate, matrix);
				}

                Platform.runLater(() -> {

                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                        	
                            double temp = matrix[i][j];
                            Color color = setColorByTemperature(temp);

                            double pixelWidth = canvas.getWidth() / 8.0;
                            double pixelHeight = canvas.getHeight() / 8.0;
                            double x = j * pixelWidth;
                            double y = i * pixelHeight;

                            gc.setFill(color);
                            gc.fillRect(x, y, pixelWidth, pixelHeight);

                            gc.setFill(Color.WHITE);
                            gc.setFont(Font.font(10));
                            String tempText = String.format("%.1f°C", temp);
                            gc.fillText(tempText, x + pixelWidth / 4, y + pixelHeight / 2);
                        }
                    }
                });

                try {
                    Thread.sleep(600);
                    for (int i = 0; i < matrix.length; i++) {
                        for (int j = 0; j < matrix[i].length; j++) {
                            matrix[i][j] = 0;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        THREAD_THERMAL_CAMERA.setDaemon(true);
        THREAD_THERMAL_CAMERA.start();
    }
    
    private Color setColorByTemperature(double temp) {
        if (temp < 0) return Color.web("#0000FF");
        else if (temp >= 0 && temp <= 20) return Color.web("#1E90FF");
        else if (temp >= 21 && temp <= 40) return Color.web("#00FFF6");
        else if (temp >= 41 && temp <= 60) return Color.web("#FFFF00");
        else if (temp >= 61 && temp <= 80) return Color.web("#FFA500");
        else if (temp > 80) return Color.web("#FF0000");
        else return Color.web("#000000");
    }
    

    // ------------------------------------
    
    public void startThermalRecording(Optional<File> directory) throws IOException {
    	try {
    	    if (!THERMAL_IS_RECORDING) {
    	    	
    	    	data_directory = directory;
    	    	thermalExcelData.createExcelFile(directory);
    	    	THERMAL_IS_RECORDING = true;
    	        container.clear();
    	        
                messageHandler.userHandlerModal(
                		"Thermal Camera", 
                		"Record started...", 
                		"When record get finished the same will be saved in: "+ directory, 
                		AlertType.CONFIRMATION
                );
    	    } else {
    	    	messageHandler.userHandlerModal(
    	    			"Thermal Camera error", 
    	    			"Reccording already started...", 
    	    			"Please, save the undegoing recording to inicialize another...", 
    	    			AlertType.WARNING
    	    	);
    	    }
		} catch (Exception e) {
	    	messageHandler.userHandlerModal(
	    			"Thermal Camera error", 
	    			e.getCause().toString(), 
	    			e.getMessage(), 
	    			AlertType.ERROR
	    	);
		}
    }
    
    public void stopThermalRecording() {
    	try {

            if (container.isEmpty()) {
            	THERMAL_IS_RECORDING = false;
            	messageHandler.userHandlerModal(
            			"Thermal Camera error", 
            			"Recording not saved...", 
            			"There is no data to be saved...", 
            			AlertType.WARNING
            	);
                return;
            }
            
            thermalExcelData.saveFile();
            
            String formattedData = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File file = new File(data_directory.get(), "Thermal_" + formattedData + ".mp4");

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(file, 320, 240);
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(20);
            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
            recorder.start();

            Java2DFrameConverter convert = new Java2DFrameConverter();
            for (double[][] frame : container) {
                BufferedImage imagem = generateImage(frame, 320, 240);
                recorder.record(convert.getFrame(imagem));
            }

            recorder.stop();
            recorder.release();
            
            recorder.close();
            convert.close();
            
            THERMAL_IS_RECORDING = false;
            data_directory = null;
            messageHandler.userHandlerModal(
                    "Thermal Camera Recording",
                    "Record stoped",
                    "The recording was saved with success...",
                    AlertType.CONFIRMATION
            );
		} catch (Exception e) {
			THERMAL_IS_RECORDING = false;
            container = null;
            data_directory = null;
            messageHandler.userHandlerModal(
                    "OV5647 Recording Error",
                    e.getCause().toString(),
                    e.getMessage(),
                    AlertType.ERROR
            );
		}
    }
    
    
    private BufferedImage generateImage(double[][] frame, int width, int height) {
        BufferedImage imagem = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();

        int pixelWidth = width / 8;
        int pixelHeight = height / 8;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                double temp = frame[i][j];
                java.awt.Color cor = convertFxColorToAwt(setColorByTemperature(temp));

                g2d.setColor(cor);
                g2d.fillRect(j * pixelWidth, i * pixelHeight, pixelWidth, pixelHeight);

                g2d.setColor(java.awt.Color.WHITE); //
                g2d.drawString(String.format("%.1f°C", temp), j * pixelWidth + 10, i * pixelHeight + 20);
            }
        }

        g2d.dispose();
        return imagem;
    }

    private java.awt.Color convertFxColorToAwt(javafx.scene.paint.Color fxColor) {
        int r = (int) (fxColor.getRed() * 255);
        int g = (int) (fxColor.getGreen() * 255);
        int b = (int) (fxColor.getBlue() * 255);
        return new java.awt.Color(r, g, b);
    }

    public void stopThermalCamera() {
    	THREAD_THERMAL_RUNNING = false;
    	if (THREAD_THERMAL_CAMERA != null) {
    	    THREAD_THERMAL_CAMERA.interrupt();
    	}
    	THREAD_THERMAL_CAMERA = null;
    }
    
}
