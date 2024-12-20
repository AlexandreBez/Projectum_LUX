package projectum_lux.raspberry;

import java.util.Arrays;
import java.util.List;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.PullResistance;
import com.pi4j.io.i2c.I2C;

import javafx.scene.control.Alert.AlertType;
import projectum_lux.helper.UserMessageHandler;

public class PeripheralsConnection {

	private UserMessageHandler messageHandler;
	private List<String> ids;
    
    //-----------------------------------------------

    public DigitalInput setupDigitalInput(String id, int address, PullResistance pullResistance, Context pi4j) {
        try {
            pi4j.din().create(DigitalInput.newConfigBuilder(pi4j)
                    .id(id)
                    .address(address)
                    .pull(pullResistance)
                    .build());
            ids = Arrays.asList(id);
		} catch (Exception e) {
			messageHandler.userHandlerModal("Digital Input error", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
		}
		return null;
    }

    public DigitalOutput setupDigitalOutput(String id, int address, DigitalOutput initialState, Context pi4j) {
        try {
            DigitalOutput digOut = pi4j.dout().create(DigitalOutput.newConfigBuilder(pi4j)
                    .id(id)
                    .address(address)
                    .initial(initialState.state())
                    .build());
            ids = Arrays.asList(id);
            return digOut;
		} catch (Exception e) {
			messageHandler.userHandlerModal("Digital Output error", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
		}
        return null;
    }

    public I2C setupI2C(String id, int bus, int deviceAddress, Context pi4j) {
        try {
            I2C i2c = pi4j.i2c().create(I2C.newConfigBuilder(pi4j)
                    .id(id)
                    .bus(bus)
                    .device(deviceAddress)
                    .build());
            
            ids = Arrays.asList(id);
            
            return i2c;
		} catch (Exception e) {
			messageHandler.userHandlerModal("I2C error", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
		}        
        return null;
    }

    
    //-----------------------------------------------
    
    public void turnOffPeripheralsConn(Context pi4j) {
        for (String id : ids) {
            if (pi4j.registry().exists(id)) {
                pi4j.registry().get(id).shutdown(pi4j);
            }
        }
        pi4j.shutdown();
    }
    
}
