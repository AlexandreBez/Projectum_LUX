package raspberry_central_data;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.io.gpio.analog.AnalogInputConfig;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class ThermalCameraApp {
    private static final int I2C_BUS = 1;  // I2C Bus (1 para Raspberry Pi)
    private static final int AMG8833_ADDRESS = 0x69;  // Endereço I2C da câmera AMG8833

    // Pinos do Servo Motor
    private static final int IN1_PIN = 17;
    private static final int IN2_PIN = 27;
    private static final int IN3_PIN = 22;
    private static final int IN4_PIN = 24;

    public static void main(String[] args) {
        // Inicializa o contexto Pi4J
        Context pi4j = Pi4J.newAutoContext();

        // Configura o I2C para a câmera AMG8833
        I2CProvider i2cProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("amg8833")
                .bus(I2C_BUS)
                .device(AMG8833_ADDRESS)
                .build();
        I2C i2c = i2cProvider.create(i2cConfig);

        // Configura os pinos GPIO para o servo motor
//        DigitalOutput in1 = pi4j.create();
//        DigitalOutput in2 = pi4j.dout().id("IN2").name("Servo IN2").address(IN2_PIN).provider("pigpio-digital-output").build();
//        DigitalOutput in3 = pi4j.dout().id("IN3").name("Servo IN3").address(IN3_PIN).provider("pigpio-digital-output").build();
//        DigitalOutput in4 = pi4j.dout().id("IN4").name("Servo IN4").address(IN4_PIN).provider("pigpio-digital-output").build();

        // Sequência de ativação do motor de passo (half-step)
        int[][] stepSequence = {
                {1, 0, 0, 0}, // Passo 1
                {1, 1, 0, 0}, // Passo 2
                {0, 1, 0, 0}, // Passo 3
                {0, 1, 1, 0}, // Passo 4
                {0, 0, 1, 0}, // Passo 5
                {0, 0, 1, 1}, // Passo 6
                {0, 0, 0, 1}, // Passo 7
                {1, 0, 0, 1}  // Passo 8
        };

        // Interface Swing
        JFrame frame = new JFrame("AMG8833 Thermal Camera & Servo Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Painel para exibir dados térmicos
        JPanel thermalPanel = new JPanel() {
        	@Override
        	protected void paintComponent(Graphics g) {
        	    super.paintComponent(g);
        	    try {
        	        // Lê os dados térmicos da câmera AMG8833
        	        byte[] buffer = new byte[64];
        	        i2c.readRegister(0x80, buffer);  // Endereço de dados térmicos

        	        // Converte byte[] para int[] manualmente
        	        int[] temperatures = new int[buffer.length];
        	        for (int i = 0; i < buffer.length; i++) {
        	            temperatures[i] = buffer[i] & 0xFF; // Converte byte para int sem sinal
        	        }

        	        // Renderiza a matriz de temperatura como um mapa de calor
        	        int cellSize = 50;
        	        for (int row = 0; row < 8; row++) {
        	            for (int col = 0; col < 8; col++) {
        	                int temp = temperatures[row * 8 + col];
        	                int color = (int) Math.min(255, temp * 2);  // Escala simples
        	                g.setColor(new Color(color, 0, 255 - color));
        	                g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
        	            }
        	        }
        	    } catch (Exception e) {
        	        e.printStackTrace();
        	    }
        	}
        };
        
        thermalPanel.setPreferredSize(new Dimension(400, 400));

        // Painel para controle do servo motor
        JPanel servoPanel = new JPanel();
        JButton rotateServoButton = new JButton("Rotacionar Servo");
        rotateServoButton.addActionListener(e -> {
            // Rotaciona o motor de passo
            try {
                for (int step = 0; step < stepSequence.length; step++) {
//                    in1.setState(stepSequence[step][0] == 1);
//                    in2.setState(stepSequence[step][1] == 1);
//                    in3.setState(stepSequence[step][2] == 1);
//                    in4.setState(stepSequence[step][3] == 1);
                    Thread.sleep(10); // Controle de velocidade
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        servoPanel.add(rotateServoButton);

        // Adiciona os painéis à janela
        frame.add(thermalPanel, BorderLayout.CENTER);
        frame.add(servoPanel, BorderLayout.SOUTH);

        // Atualiza o painel de dados térmicos periodicamente
        Timer timer = new Timer(100, e -> thermalPanel.repaint());
        timer.start();

        // Exibe a janela
        frame.setVisible(true);
    }
}
