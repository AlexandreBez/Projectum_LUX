package raspberry_central_data;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;

import raspberry_central_data.panels.MotorControlPanel;
import raspberry_central_data.panels.RecordingControlPanel;
import raspberry_central_data.panels.StopwatchPanel;
import raspberry_central_data.panels.ThermalCameraPanel;

public class MainApp {
	
    public static void main(String[] args) throws Exception {
    	
        // Cria o Context do Pi4J
        Context pi4j = Pi4J.newAutoContext();
    	
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "INFO");

        JFrame mainFrame = new JFrame();

        // Configurações principais
        mainFrame.setTitle("Projeto LUX - Controle e Análise de Dados");
        mainFrame.setSize(900, 700);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setResizable(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Cria a instância única do StopwatchPanel
        StopwatchPanel stopwatchPanel = new StopwatchPanel();

        // Adiciona os controles
        JPanel controlPanel = new JPanel(new GridLayout(1, 3));
        controlPanel.add(new RecordingControlPanel(mainFrame, stopwatchPanel));
        controlPanel.add(stopwatchPanel);
        controlPanel.add(new MotorControlPanel(pi4j));
        mainFrame.add(controlPanel, BorderLayout.NORTH);

        // Adiciona o painel da câmera
        mainFrame.add(new ThermalCameraPanel(pi4j), BorderLayout.CENTER);

        // Torna a janela visível
        mainFrame.setVisible(true);
        
        // Finaliza o Context quando o programa for fechado
        Runtime.getRuntime().addShutdownHook(new Thread(pi4j::shutdown));
    }
}
