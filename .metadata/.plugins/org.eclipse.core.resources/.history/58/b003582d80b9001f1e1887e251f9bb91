package raspberry_central_data.panels;

import com.pi4j.context.Context;

import raspberry_central_data.configs.AMG8833Config;
import raspberry_central_data.core.ThermalData;

import javax.swing.*;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class ThermalCameraPanel extends JPanel {

    private AMG8833Config amg8833Config = new AMG8833Config();
    private ThermalData thermalData = new ThermalData();
    private boolean cameraActive = true;
    private final Timer updateTimer;

    public ThermalCameraPanel(Context pi4j) {

    	this.updateTimer = new Timer();
    	
    	amg8833Config.AMG8833InterruptConfig(pi4j).addListener(event -> {
    		
            if (event.state().isHigh() && cameraActive) {
                double maxTemperature = thermalData.getMaxTemperature();
                if (maxTemperature >= 75 && maxTemperature < 80) {
                	
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "Atenção! A temperatura atingiu " + maxTemperature + "°C.\nPor favor, monitore o sistema.",
                                "Aviso de Temperatura",
                                JOptionPane.WARNING_MESSAGE
                        );
                    });
                    
                } else if (maxTemperature >= 80) {
                    showCriticalTemperatureDialog();
                    cameraActive = false;
                    updateTimer.cancel();
                }
            }
            
        });


        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(900, 700));

        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (cameraActive) {
                	thermalData.updateTemperatures(amg8833Config, pi4j);
                    SwingUtilities.invokeLater(() -> repaint());
                }
            }
        }, 0, 500);
        
    }
    
    private void showCriticalTemperatureDialog() {
    	
        JFrame criticalFrame = new JFrame("Alerta Crítico - Alta Temperatura");
        criticalFrame.setSize(400, 300);
        criticalFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        criticalFrame.setLayout(new GridLayout(3, 1));

        JLabel messageLabel = new JLabel(
                "<html><center>Temperatura Crítica!<br>A temperatura ultrapassou 80°C.</center></html>",
                JLabel.CENTER
        );

        JButton validateTemperatureButton = new JButton("Validar Temperatura");
        
        validateTemperatureButton.addActionListener(e -> {
        	
            double currentTemperature = thermalData.getMaxTemperature();
            
            if (currentTemperature <= 70) {
            	
                JOptionPane.showMessageDialog(
                        criticalFrame,
                        "Temperatura estabilizada em " + currentTemperature + "°C. Sistema pode continuar.",
                        "Temperatura Estabilizada",
                        JOptionPane.INFORMATION_MESSAGE
                );
                
                criticalFrame.dispose();
            } else {
            	
                JOptionPane.showMessageDialog(
                        criticalFrame,
                        "Temperatura ainda crítica: " + currentTemperature + "°C. Aguarde mais tempo.",
                        "Atenção",
                        JOptionPane.WARNING_MESSAGE
                );
                
            }
        });

        criticalFrame.add(messageLabel);
        criticalFrame.add(validateTemperatureButton);
        criticalFrame.setLocationRelativeTo(null);
        criticalFrame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int cellWidth = getWidth() / 8;
        int cellHeight = getHeight() / 8;

        // Determinar valores mínimo e máximo da matriz de temperaturas
        double minTemp = Double.MAX_VALUE;
        double maxTemp = Double.MIN_VALUE;

        for (double[] row : thermalData.temperatures) {
            for (double temp : row) {
                minTemp = Math.min(minTemp, temp);
                maxTemp = Math.max(maxTemp, temp);
            }
        }

        // Renderizar a matriz de calor
        for (int row = 0; row < thermalData.temperatures.length; row++) {
            for (int col = 0; col < thermalData.temperatures[row].length; col++) {
                double temp = thermalData.temperatures[row][col];
                int colorValue = (int) ((temp - minTemp) / (maxTemp - minTemp) * 255);
                g.setColor(new Color(colorValue, 0, 255 - colorValue));
                g.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
            }
        }

        // Renderizar a legenda
        int legendHeight = getHeight();
        int legendWidth = 50;
        int legendX = getWidth() - legendWidth - 10;
        int legendY = 10;

        g.setColor(Color.WHITE);
        g.fillRect(legendX, legendY, legendWidth, legendHeight - 20);

        // Adicionar gradiente e valores de temperatura na legenda
        for (int i = 0; i < legendHeight - 20; i++) {
            double temp = maxTemp - ((i / (double) (legendHeight - 20)) * (maxTemp - minTemp));
            int colorValue = (int) ((temp - minTemp) / (maxTemp - minTemp) * 255);

            g.setColor(new Color(colorValue, 0, 255 - colorValue));
            g.drawLine(legendX, legendY + i, legendX + legendWidth, legendY + i);

            // Adicionar rótulos de temperatura em intervalos
            if (i % 50 == 0) {
                g.setColor(Color.BLACK);
                g.drawString(String.format("%.1f°C", temp), legendX + legendWidth + 5, legendY + i);
            }
        }
    }

}
