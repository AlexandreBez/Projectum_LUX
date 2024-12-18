package raspberry_central_data.panels;

import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;

import raspberry_central_data.core.ThermalData;

import javax.swing.*;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

@SuppressWarnings("serial")
public class ThermalCameraPanel extends JPanel {

	private static final int I2C_BUS = 1;
	private static final int AMG8833_ADDRESS = 0x68;
	private static final int INTERRUPT_PIN = 24;
	public double[][] FRAME = new double[8][8];

    private ThermalData thermalData;
    private I2C i2c;
    private DigitalInput interrupt;
    private boolean cameraActive = true;
    private final Timer updateTimer;
    

    public ThermalCameraPanel(com.pi4j.context.Context context) {
    	
    	i2c = context.i2c().create(I2C.newConfigBuilder(context)
				.id("amg8833")
				.bus(I2C_BUS)
				.device(AMG8833_ADDRESS)
				.build());
    	
    	interrupt = context.din().create(DigitalInput.newConfigBuilder(context)
				.id("amg8833-int")
				.address(INTERRUPT_PIN)
				.pull(PullResistance.PULL_DOWN) 
				.provider("gpiod-digital-input")
				.build());

        this.thermalData = new ThermalData();
    	this.updateTimer = new Timer();
    	
    	interrupt.addListener(event -> {
    		
            if (event.state().isHigh() && cameraActive) {
                double maxTemperature = thermalData.getMaxTemperature();
                if (maxTemperature >= 75 && maxTemperature < 80) {
                	
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                this,
                                "AtenÃ§Ã£o! A temperatura atingiu " + maxTemperature + "Â°C.\nPor favor, monitore o sistema.",
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
                	thermalData.updateTemperatures(i2c, FRAME);
                    SwingUtilities.invokeLater(() -> repaint());
                }
            }
        }, 0, 1000);
        
    }
    
    private void showCriticalTemperatureDialog() {
    	
        JFrame criticalFrame = new JFrame("Alerta CrÃ­tico - Alta Temperatura");
        criticalFrame.setSize(400, 300);
        criticalFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        criticalFrame.setLayout(new GridLayout(3, 1));

        JLabel messageLabel = new JLabel(
                "<html><center>Temperatura CrÃ­tica!<br>A temperatura ultrapassou 80Â°C.</center></html>",
                JLabel.CENTER
        );

        JButton validateTemperatureButton = new JButton("Validar Temperatura");
        
        validateTemperatureButton.addActionListener(e -> {
        	
            double currentTemperature = thermalData.getMaxTemperature();
            
            if (currentTemperature <= 70) {
            	
                JOptionPane.showMessageDialog(
                        criticalFrame,
                        "Temperatura estabilizada em " + currentTemperature + "Â°C. Sistema pode continuar.",
                        "Temperatura Estabilizada",
                        JOptionPane.INFORMATION_MESSAGE
                );
                
                criticalFrame.dispose();
            } else {
            	
                JOptionPane.showMessageDialog(
                        criticalFrame,
                        "Temperatura ainda crÃ­tica: " + currentTemperature + "Â°C. Aguarde mais tempo.",
                        "AtenÃ§Ã£o",
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

        // Evitar divisões por zero
        double range = maxTemp - minTemp;
        if (range == 0) {
            range = 1; // Evitar divisão por zero
        }

        // Renderizar a matriz de calor
        for (int row = 0; row < thermalData.temperatures.length; row++) {
            for (int col = 0; col < thermalData.temperatures[row].length; col++) {
                double temp = thermalData.temperatures[row][col];
                int colorValue = (int) ((temp - minTemp) / range * 255); // Normalização
                g.setColor(new Color(colorValue, 0, 255 - colorValue));
                g.fillRect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
            }
        }

        // Renderizar a legenda
        int legendHeight = getHeight() - 20;
        int legendWidth = 50;
        int legendX = getWidth() - legendWidth - 10;
        int legendY = 10;

        g.setColor(Color.WHITE);
        g.fillRect(legendX, legendY, legendWidth, legendHeight);

        // Adicionar gradiente e valores de temperatura na legenda
        for (int i = 0; i < legendHeight; i++) {
            double temp = maxTemp - ((i / (double) legendHeight) * range);
            int colorValue = (int) ((temp - minTemp) / range * 255);

            g.setColor(new Color(colorValue, 0, 255 - colorValue));
            g.drawLine(legendX, legendY + i, legendX + legendWidth, legendY + i);

            // Adicionar rótulos de temperatura em intervalos regulares
            if (i % (legendHeight / 10) == 0) {
                g.setColor(Color.BLACK);
                g.drawString(String.format("%.1f°C", temp), legendX + legendWidth + 5, legendY + i);
            }
        }
    }


}
