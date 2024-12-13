package raspberry_central_data.core;

import com.pi4j.io.i2c.I2C;

public class ThermalData {
	
	public ThermalData() {};
	public double[][] temperatures;

	public double[][] updateTemperatures(I2C i2c, double[][] frame) {
	    try {
	        temperatures = frame;
	        byte[] buffer = new byte[64 * 2];
	        i2c.readRegister(0x80, buffer, 0, buffer.length);

	        for (int i = 0; i < 64; i++) {
	            int tempRaw = ((buffer[i * 2 + 1] & 0xFF) << 8) | (buffer[i * 2] & 0xFF);
	            temperatures[i / 8][i % 8] = tempRaw * 0.25; // Certifique-se de que este valor está correto
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    return temperatures;
	}
	
	public double getMaxTemperature() {
        double maxTemp = Double.MIN_VALUE;
        for (double[] row : temperatures) {
            for (double temp : row) {
                maxTemp = Math.max(maxTemp, temp);
            }
        }
        return maxTemp;
    }
}

