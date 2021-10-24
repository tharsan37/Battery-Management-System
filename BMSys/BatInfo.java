import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.Font;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

public class BatInfo {

	private JFrame frame;//create information GUI
	JButton btnSchema = new JButton("Schema");
	JPanel panel = new JPanel();
	JButton btnManual = new JButton("Handbuch");
	JButton btnTurnOff = new JButton("Ausschalten");
	JButton btnBack = new JButton("Zurueck");
	JTextPane txtpnInfo = new JTextPane();

	public BatInfo() {

		frame = new JFrame();
		frame.setBounds(100, 50, 600, 380);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel, BorderLayout.CENTER);

		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		btnSchema.addActionListener(new ActionListener() {// ---------Button Schema--->>
			public void actionPerformed(ActionEvent e) {
				File file = new File("/home/pi/Desktop/code/Data/Schema.pdf");
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		btnSchema.setBounds(412, 21, 141, 35);
		panel.add(btnSchema);
		
		btnManual.addActionListener(new ActionListener() {// ---------Button Manual--->>
			public void actionPerformed(ActionEvent e) {
				File file = new File("/home/pi/Desktop/code/Data/Manual.pdf");
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		btnManual.setBounds(412, 77, 141, 35);
		panel.add(btnManual);
		
		btnTurnOff.addActionListener(new ActionListener() {// ---------Button TurnOff--->>
			public void actionPerformed(ActionEvent arg0) {
				BatControl.TurnOffRelay();
				System.exit(0);
			}
		});

		btnTurnOff.setBounds(412, 133, 141, 35);
		panel.add(btnTurnOff);
		
		btnBack.addActionListener(new ActionListener() {// ---------Button Back--->>
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
				frame.dispose();
			}
		});

		btnBack.setBounds(412, 253, 141, 35);
		panel.add(btnBack);
		
		txtpnInfo.setForeground(Color.BLACK);// ---------TextPanel Info--->>
		txtpnInfo.setEditable(false);
		txtpnInfo.setRequestFocusEnabled(false);
		txtpnInfo.setFont(new Font("Tahoma", Font.PLAIN, 18));
		txtpnInfo.setText(
				"Battery Management System\r\n(BMSys)\r\nDiplomarbeit (taaratha Q4,Teko2021)\r\nVersion 1.0\r\nTharsan Ravitharan");
		txtpnInfo.setBounds(21, 21, 291, 267);
		panel.add(txtpnInfo);

	}

}
