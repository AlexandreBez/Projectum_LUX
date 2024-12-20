package projectum_lux.data_control;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import projectum_lux.helper.UserMessageHandler;

public class OV5647Data {
	
	private UserMessageHandler messageHandler;
	
	public boolean THREAD_OV5647_RUNNING = false;
	public boolean OV5647_is_recording = false;
	
	public Thread THREAD_OV5647_UPDATE_CANVAS;
    private Java2DFrameConverter conversion;
    private FFmpegFrameRecorder recorder;
	
    public void updateCanvasWithOV5647Data(Canvas canvas, OpenCVFrameGrabber grabber) {
    	
        if (THREAD_OV5647_RUNNING) {
            return;
        }

        THREAD_OV5647_RUNNING = true;

        GraphicsContext gc = canvas.getGraphicsContext2D();

        THREAD_OV5647_UPDATE_CANVAS = new Thread(() -> {
            while (THREAD_OV5647_RUNNING) {
                try {
                    Frame frame = grabber.grab();
                    if (frame != null) {
                        BufferedImage bufferedImage = conversion.convert(frame);
                        WritableImage writableImage = bufferedImageToWritableImage(bufferedImage);

                        Platform.runLater(() -> {
                            gc.drawImage(writableImage, 0, 0, canvas.getWidth(), canvas.getHeight());
                        });

                        if (OV5647_is_recording) {
                            recorder.record(frame);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao capturar ou renderizar frame: " + e.getMessage());
                }

                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        THREAD_OV5647_UPDATE_CANVAS.setDaemon(true);
        THREAD_OV5647_UPDATE_CANVAS.start();
    }

    private WritableImage bufferedImageToWritableImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;

        WritableImage writableImage = new WritableImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                pixelWriter.setColor(x, y, Color.rgb(red, green, blue));
            }
        }

        return writableImage;
    }
    
    public void startOV5647Record(OpenCVFrameGrabber grabber, Optional<File> directory) {
        try {
		    if (!OV5647_is_recording) {
	            String nomeArquivo = "OV5647_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".mp4";
	            File arquivoVideo = new File(directory.get(), nomeArquivo);

	            recorder = new FFmpegFrameRecorder(arquivoVideo, grabber.getImageWidth(), grabber.getImageHeight());
	            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
	            recorder.setFormat("mp4");
	            recorder.setFrameRate(20);
	            recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
	            recorder.start();

	            OV5647_is_recording = true;
	            messageHandler.userHandlerModal(
	            		"OV5647 Camera", 
	            		"Record started...", 
	            		"When record get finished the same will be saved in: "+ arquivoVideo.getAbsolutePath(), 
	            		AlertType.CONFIRMATION
	            );
		    } else {
		    	messageHandler.userHandlerModal(
		    			"OV5647 Camera error", 
		    			"Reccording already started...", 
		    			"Please, save the undegoing recording to inicialize another...", 
		    			AlertType.WARNING
		    	);
		    }
        } catch (Exception e) {
	    	messageHandler.userHandlerModal(
	    			"OV5647 Camera error", 
	    			e.getCause().toString(), 
	    			e.getMessage(), 
	    			AlertType.ERROR
	    	);
        }
    }
    
    public void stopOV5647CameraRecord() {
        try {
            if (OV5647_is_recording) {
            	
                recorder.stop();
                recorder.release();
                recorder = null;

                OV5647_is_recording = false;
                messageHandler.userHandlerModal(
                    "OV5647 Camera Recording",
                    "Record stoped",
                    "The recording was saved with success...",
                    AlertType.CONFIRMATION
                );
            } else {
                messageHandler.userHandlerModal(
                        "OV5647 Recording Error",
                        null,
                        "No recording in progress...",
                        AlertType.WARNING
                );
            }
        } catch (Exception e) {
        	OV5647_is_recording = false;
            recorder = null;
            messageHandler.userHandlerModal(
                    "OV5647 Recording Error",
                    e.getCause().toString(),
                    e.getMessage(),
                    AlertType.ERROR
            );
        }
    }

}
