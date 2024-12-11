package raspberry_central_data.panels;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class RecordingControlPanel extends JPanel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private JFrame parent;

    public RecordingControlPanel(JFrame parent, StopwatchPanel stopwatchPanel) {
        
        this.parent = parent;
        setLayout(new FlowLayout());
        
        JLabel actualDate = new JLabel(new Date().toString());
        JButton startRecordingButton = new JButton("Iniciar Gravação");
        JButton stopRecordingButton = new JButton("Parar Gravação");

        // Ação para iniciar a gravação
        startRecordingButton.addActionListener(e -> {
        	stopwatchPanel.start();
            JOptionPane.showMessageDialog(this, "Gravação iniciada.");
        });

        // Ação para parar a gravação
        stopRecordingButton.addActionListener(e -> {
        	stopwatchPanel.stop();
        	new SaveDialogPanel(parent);
        });

        add(actualDate);
        add(startRecordingButton);
        add(stopRecordingButton);
    }
}
