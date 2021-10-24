import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BatConfig {

	private JFrame frame1;// --------------------------------Create Objects------------>>
	static int[] ConfDat = { 1, 2 };
	JPanel panel = new JPanel();
	JLabel lblNumbOfRow = new JLabel("Anzahl Reihen:");
	JComboBox comboBoxRow = new JComboBox();
	JLabel lblNumbOfBat = new JLabel("Anzahl Batterien pro Reihe:");
	JLabel lblTotVoltage = new JLabel("Empfohlene Ladespannung:");	
	JComboBox comboBoxBat = new JComboBox();
	JButton btnCheckConfig = new JButton("Pruefen");
	JLabel lblCheckConfig = new JLabel("Bitte Batterie-Konfiguration eingeben...");
	JButton btnApply = new JButton("Anwenden");
	JButton btnBack = new JButton("Zurueck");
	
	public BatConfig() {

		frame1 = new JFrame();//create configuration GUI
		frame1.setBounds(100, 50, 600, 380);
		frame1.setVisible(true);
		frame1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame1.getContentPane().add(panel, BorderLayout.CENTER);

		panel.setLayout(null);

		lblNumbOfRow.setBounds(21, 21, 161, 26); // ----------lblNumbOfRow------>>
		panel.add(lblNumbOfRow);

		lblNumbOfBat.setBounds(21, 113, 262, 26); // ---------lblNumbOfBat------>>
		panel.add(lblNumbOfBat);

		lblCheckConfig.setBounds(331, 78, 222, 97); // -------lblCheckConfig---->>
		panel.add(lblCheckConfig);
		

		lblTotVoltage.setBounds(21, 205, 290, 26);// -------lblTotVoltage---->>
		panel.add(lblTotVoltage);
		
		comboBoxRow.addItemListener(new ItemListener() { // -----------ComboBoxRow----->>
			public void itemStateChanged(ItemEvent e) {
				lblCheckConfig.setText("Bitte Pruefen lassen!");
				btnApply.setEnabled(false);

			}
		});
		comboBoxRow.setModel(new DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8" }));
		comboBoxRow.setBounds(21, 57, 60, 35);
		comboBoxRow.setSelectedIndex(ConfDat[0] - 1);
		panel.add(comboBoxRow);

		comboBoxBat.addItemListener(new ItemListener() { // -----------ComboBoxBat----->>
			public void itemStateChanged(ItemEvent e) {
				lblCheckConfig.setText("Bitte Pruefen lassen!");
				btnApply.setEnabled(false);
				lblTotVoltage.setText("Empfohlene Ladespannung " + (14*(comboBoxBat.getSelectedIndex() + 2))+ " V");
			}
		});
		comboBoxBat.setModel(new DefaultComboBoxModel(new String[] { "2", "3", "4", "5", "6", "7", "8" }));
		comboBoxBat.setBounds(21, 149, 60, 35);
		comboBoxBat.setSelectedIndex(ConfDat[1] - 2);
		panel.add(comboBoxBat);

		btnCheckConfig.addMouseListener(new MouseAdapter() { // -------Button Check---->>
			@Override
			public void mouseClicked(MouseEvent e) {
				// If the Configuration equals less than 16 Battery, ApplyButton gets active:
				if ((comboBoxRow.getSelectedIndex() + 1) * (comboBoxBat.getSelectedIndex() + 2) <= 16) {
					btnApply.setEnabled(true);
					lblCheckConfig.setText("<html>Die Konfigurationen<br/>enthalten keine Fehler.</html>");
					
					

				} else {
					lblCheckConfig.setText("Fehler! Zu viele Batterien.");
				}
			}
		});
		btnCheckConfig.setBounds(21, 253, 141, 35);
		panel.add(btnCheckConfig);

		btnApply.addActionListener(new ActionListener() { // ----------Button Apply---->>
			public void actionPerformed(ActionEvent e) {
				ConfDat[0] = (comboBoxRow.getSelectedIndex() + 1);
				ConfDat[1] = (comboBoxBat.getSelectedIndex() + 2);
				lblCheckConfig.setText("Die Konfigurationen sind aktiv.");
				try {
					BatControl.SetConfig(ConfDat);// -------Save ConfDat to File
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
				BMSys.NumbOfBat = BMSys.BuildGui(ConfDat);// ------------Build GUI with new ConfDat
			}
		});
		btnApply.setEnabled(false);
		btnApply.setBounds(250, 253, 141, 35);
		panel.add(btnApply);

		btnBack.addActionListener(new ActionListener() { // ---------Button Back--->>
			public void actionPerformed(ActionEvent e) {
				frame1.setVisible(false);
				frame1.dispose();
			}
		});
		btnBack.setBounds(412, 253, 141, 35);
		panel.add(btnBack);
		
		
	}
}
