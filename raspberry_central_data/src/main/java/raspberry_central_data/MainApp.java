package raspberry_central_data;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

import raspberry_central_data.panels.RecordingControlPanel;
import raspberry_central_data.panels.StopwatchPanel;
import raspberry_central_data.panels.ThermalCameraPanel;

/**
 * Inicializa atraves do main o context, panels e etc
 */
public class MainApp {
	
	
    public static void main(String[] args) throws Exception {
    	
    	// slf4j para o pi4j
    	System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");
    	
    	Context context = Pi4J.newAutoContext();
    	ThermalCameraPanel cameraPanel = new ThermalCameraPanel(context);
        JFrame mainFrame = new JFrame();

        // ConfiguraÃ§Ãµes principais
        mainFrame.setTitle("Projeto LUX - Controle e AnÃ¡lise de Dados");
        mainFrame.setSize(900, 700);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setResizable(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Cria a instÃ¢ncia Ãºnica do StopwatchPanel
        StopwatchPanel stopwatchPanel = new StopwatchPanel();

        // Adiciona os controles
        JPanel controlPanel = new JPanel(new GridLayout(1, 3));
        controlPanel.add(new RecordingControlPanel(mainFrame, stopwatchPanel, cameraPanel));
        controlPanel.add(stopwatchPanel);
        mainFrame.add(controlPanel, BorderLayout.NORTH);

        // Adiciona o painel da cÃ¢mera
        mainFrame.add(cameraPanel, BorderLayout.CENTER);

        // Torna a janela visÃ­vel
        mainFrame.setVisible(true);
        
        // Finaliza o Context quando o programa for fechado
        Runtime.getRuntime().addShutdownHook(new Thread(context::shutdown));
    }
}
