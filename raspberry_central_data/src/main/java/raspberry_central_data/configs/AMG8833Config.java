package raspberry_central_data.configs;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;

public class AMG8833Config {
	
    private static final int I2C_BUS = 1;
    private static final int AMG8833_ADDRESS = 0x68;
    private static final int INTERRUPT_PIN = 24;
    public double[][] FRAME = new double[8][8];
    
	public AMG8833Config() {}

	public I2C AMG8833I2CConfig(Context context) {
		return context.i2c().create(I2C.newConfigBuilder(context)
				.id("amg8833")
				.bus(I2C_BUS)
				.device(AMG8833_ADDRESS)
				.build());
	};
	
	public DigitalInput AMG8833InterruptConfig(Context context) {
		return context.din().create(DigitalInput.newConfigBuilder(context)
				.id("amg8833-int")
				.address(INTERRUPT_PIN)
				.pull(PullResistance.PULL_DOWN) 
				.provider("gpiod-digital-input")
				.build());
	};
	
}
