package raspberry_central_data.core;

import com.pi4j.context.Context;

import raspberry_central_data.configs.AMG8833Config;

public class ThermalData {
	
	public ThermalData() {};
	public double[][] temperatures;

	public double[][] updateTemperatures(AMG8833Config amg8833Config, Context context) {
        try {
        	temperatures = amg8833Config.FRAME;
            byte[] buffer = new byte[64 * 2];
            amg8833Config.AMG8833I2CConfig(context).readRegister(0x80, buffer, 0, buffer.length);

            for (int i = 0; i < 64; i++) {
                int tempRaw = ((buffer[i * 2 + 1] & 0xFF) << 8) | (buffer[i * 2] & 0xFF);
                temperatures[i / 8][i % 8] = tempRaw * 0.25;
            }
            //monitorCriticalTemperatures();
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