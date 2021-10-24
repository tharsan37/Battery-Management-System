import java.awt.EventQueue;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BMSys {
	// initialization of GPIO from RasPi
	public static SpiDevice spi = null;
	public static SpiDevice spi2 = null;

	private JFrame frame;

//----------------------------------------------main method--------------------------<<
	public static void main(String args[]) throws InterruptedException, IOException {
		System.out.println("Starting BMSys Application");
		spi = SpiFactory.getInstance(SpiChannel.CS0, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
		spi2 = SpiFactory.getInstance(SpiChannel.CS1, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BMSys window = new BMSys();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// -----------------------------------------start_of_application-----------------<<
	public BMSys() {
		initialize();
		new Thread(new Runnable() {
			@Override
			public void run() {

				while (true) {

					try {
						Thread.sleep(10); // execution rate of BackgroundProcess in ms
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						BackgroundProcess();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

//-------------------------------------------------create objects--------------------<<
	static JLabel lblChargeMode = new JLabel("Modus AUS");
	static JPanel panelChargeMode = new JPanel();
	static JButton btnInfo = new JButton("Info");
	static JLabel lblTime = new JLabel("");
	static Battery[] battery = { new Battery(), new Battery(), new Battery(), new Battery(), new Battery(),
			new Battery(), new Battery(), new Battery(), new Battery(), new Battery(), new Battery(), new Battery(),
			new Battery(), new Battery(), new Battery(), new Battery() };

	// --------------------------------------------initialization--------------------<<
	private void initialize() {
		frame = new JFrame();// Create Main GUI
		frame.setBounds(0, 0, 800, 480);
		frame.setUndecorated(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(MainPanel, BorderLayout.CENTER);

		MainPanel.setLayout(null);
		lblBMSys.setBounds(21, 21, 269, 26);
		MainPanel.add(lblBMSys);

		BatControl.SetRelayArray();

		for (int i = 0; i < battery.length; i++) { // initialize all Batteries to MainPanel
			MainPanel.add(battery[i]);
		}

		JButton btnConfiguration = new JButton("Konfiguration");// initialize btnConfiguration:
		btnConfiguration.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent arg0) {
				BatConfig bc = new BatConfig();
			}
		});
		btnConfiguration.setBounds(622, 424, 157, 35);
		MainPanel.add(btnConfiguration);

		btnInfo.addActionListener(new ActionListener() {// initialize btnInfo:
			public void actionPerformed(ActionEvent arg0) {
				BatInfo bi = new BatInfo();
			}
		});
		btnInfo.setBounds(622, 368, 157, 35);
		MainPanel.add(btnInfo);

		btnChargeMode.addActionListener(new ActionListener() {// initialize btnChargeMode:
			public void actionPerformed(ActionEvent e) {
				StatChargeMode = BatControl.ChargeMode(StatChargeMode);
				CountBackgroundProcess = 3590;
				for (int i = 0; i < AnalogData.length; i++) { // set all AnalogData values to 0
					AnalogData[i] = 0;
				}

			}
		});
		btnChargeMode.setBounds(444, 368, 157, 35);
		MainPanel.add(btnChargeMode);

		panelChargeMode.setBackground(Color.GREEN);// initialize panelChargeMode:
		panelChargeMode.setBounds(444, 424, 157, 35);
		MainPanel.add(panelChargeMode);

		panelChargeMode.add(lblChargeMode);// initialize lblChargeMode:

		lblTime.setBounds(444, 321, 335, 26);// initialize lblTime:
		MainPanel.add(lblTime);

		try { // Load ConfDat before starting
			BatConfig.ConfDat = BatControl.GetConfig();
			BatControl.SetConfig(BatConfig.ConfDat);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		NumbOfBat = BuildGui(BatConfig.ConfDat);// build GUI before starting
	}

	// --------------------------------------------------------------------custom_variables
	int CountBackgroundProcess = 1;
	int CountTime = 0;
	int[] RelayStatus = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// 1=relay is closed, 0=relay is open
	static int NumbOfBat = 1;// number of used battery
	static int[] AnalogData = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// Analog data value between 0 and 4096
	static double[] VoltageValues = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// Voltage value for each battery
	boolean StatChargeMode = false;// status if charge mode is on (false=off, true=on)
	static JPanel MainPanel = new JPanel();
	static JLabel lblBMSys = new JLabel("Battery Management System");
	private final JButton btnChargeMode = new JButton("Lade Modus");

	// -----------------------------------------------------------------------------BackgroundProcess()------------------>>
	private void BackgroundProcess() throws IOException {
		if (StatChargeMode == false) {// ----code if charge mode is of:>>
			AnalogData = BatControl.GetAnalogData(AnalogData);// get analog data every 10 ms
			if (CountBackgroundProcess >= 400) {// these loop executes every 4 seconds
				CountBackgroundProcess = 1;
				VoltageValues = BatControl.CalcAnalogData(AnalogData);
				RelayStatus = BatControl.SetRelay(StatChargeMode, VoltageValues, NumbOfBat);
				VoltagePrint(VoltageValues, RelayStatus);
			}
		} else {// ----code if charge mode is on:>>
			if (CountTime >= 100) {// countdown for measurement
				CountTime = 0;
				TimePrint(39 - (CountBackgroundProcess / 100));
			}

			if (CountBackgroundProcess == 3590) {// turn off all relay before measurement
				BatControl.TurnOffRelay();
			}
			if (CountBackgroundProcess >= 3601) {// collect analog data over 4 seconds, takes effect every 36 seconds
				AnalogData = BatControl.GetAnalogData(AnalogData);
			}

			if (CountBackgroundProcess >= 4000) {// print Data to GUI all 40 seconds
				CountBackgroundProcess = 1;
				VoltageValues = BatControl.CalcAnalogData(AnalogData);
				RelayStatus = BatControl.SetRelay(StatChargeMode, VoltageValues, NumbOfBat);
				VoltagePrint(VoltageValues, RelayStatus);
			}
		}
		CountBackgroundProcess++;
		CountTime++;
	}

	// -----------------------------------------------------------------------------BuildGui()--------------------------->>
	public static int BuildGui(int[] array) {

		int[] AxleX = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// Axle x of all battery
		int[] AxleY = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// Axle y of all battery
		int ConNumb = 0; // initialize connection Number and set to 0
		int NumbOfBat = array[0] * array[1];// calculate amount of connected battery

		for (int i = 0; i < array[0]; i++) { // fill in the axle values
			for (int j = 0; j < array[1]; j++) {
				AxleX[ConNumb] = 20 + (j * 90);
				AxleY[ConNumb] = 60 + (i * 50);
				ConNumb++;
			}
		}
		for (int i = 0; i < battery.length; i++) {// set all battery invisible
			battery[i].setVisible(false);
		}
		for (int i = 0; i < NumbOfBat; i++) {
			battery[i].setVisible(true);// set used battery visible
			battery[i].setLocation(AxleX[i], AxleY[i]);// set the locations of each Battery
		}
		return NumbOfBat;
	}

	// -----------------------------------------------------------------------------VoltagePrint()----------------------->>
	public static void VoltagePrint(double[] output, int[] StatusArray) {
		String formattedData;// string to print out
		for (int i = 0; i < battery.length; i++) {
			if (output[i] <= 2) { // if battery isn't connected
				battery[i].pnlBat.setBackground(Color.RED);
				battery[i].lblText.setText("Fehler!");
			}
			if (output[i] >= 2) {// if battery is low (under 12V)
				battery[i].pnlBat.setBackground(Color.ORANGE);
				formattedData = String.format("%.02f", output[i]);
				battery[i].lblText.setText(formattedData + "V");
			}
			if (output[i] >= 12) {// if battery is i.O. (over 12V)
				battery[i].pnlBat.setBackground(Color.GREEN);
				formattedData = String.format("%.02f", output[i]);
				battery[i].lblText.setText(formattedData + "V");
			}
			if (StatusArray[i] == 1) {// if relay of battery is closed
				battery[i].pnlBat.setBackground(Color.MAGENTA);
			}
		}
		for (int i = 0; i < AnalogData.length; i++) { // set all AnalogData values to 0
			AnalogData[i] = 0;
		}
	}

	// -----------------------------------------------------------------------------TimePrint()----------------------->>
	public static void TimePrint(int Time) {
		double DoubleTime = (double) Time;
		String formattedData;
		formattedData = String.format("%.0f", DoubleTime);
		lblTime.setText("Naechste Messung in: " + formattedData + " sek");
	}
}
