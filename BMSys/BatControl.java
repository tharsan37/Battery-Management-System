import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class BatControl {
	// define the GPIO pins and save them in the ArrayList
	public static final ArrayList<Pin> PINS = new ArrayList<Pin>(Arrays.asList(RaspiPin.GPIO_00, RaspiPin.GPIO_01,
			RaspiPin.GPIO_02, RaspiPin.GPIO_03, RaspiPin.GPIO_04, RaspiPin.GPIO_05, RaspiPin.GPIO_06, RaspiPin.GPIO_07,
			RaspiPin.GPIO_21, RaspiPin.GPIO_22, RaspiPin.GPIO_23, RaspiPin.GPIO_24, RaspiPin.GPIO_25, RaspiPin.GPIO_26,
			RaspiPin.GPIO_27, RaspiPin.GPIO_28));

	private static ArrayList<GpioPinDigitalOutput> relays = new ArrayList<GpioPinDigitalOutput>();
	private final static GpioController gpio = GpioFactory.getInstance();

	public static void SetRelayArray() {
		for (int i = 0; i < 16; i++) {
			relays.add(gpio.provisionDigitalOutputPin(PINS.get(i), "relay" + i, PinState.HIGH));// initialize output
																								// pins
		}

	}

	// -----------------------------------------------------------------------------GetAnalogData()-------------------->>
	// the code in "GetAnalogData() is copied from "Bikash Narayan Panda" see
	// Diplomarbeit_BMSys https://github.com/oksbwn/MCP3208_Raspberry-Pi/blob/master/MCP3208_raspberryPi.java.
	// this code is modified form  taaratha.
	public static int[] GetAnalogData(int[] oldValues) throws IOException {
		int[] array = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };// all analog Data will be saved in this array
// ----first MCP3208 (spi):---->>
		for (short channel = 0; channel < 8; channel++) {
			byte data[] = new byte[] { (byte) 0b00000000, // first byte, with start bit
					(byte) ((byte) channel << 6), (byte) 0b00000000 // third byte transmitted....don't care
			};
			if (channel > 3)
				data[0] = 0B00000111; // first Byte for Channel 3-7
			else
				data[0] = 0B00000110;// first Byte for Channel 0-3

			byte[] result = BMSys.spi.write(data); // request data from MCP3208 via SPI

			int value = (result[1] << 8) & 0b0000111111111111; // merge data[1] & data[2] to get 10-bit result
			value |= (result[2] & 0xff);
			array[channel] = value + oldValues[channel];
			// System.out.println(value); //test if analog number is necessary
		}
// ----second MCP3208 (spi2):---->>
		for (short channel = 0; channel < 8; channel++) {
			byte data[] = new byte[] { (byte) 0b00000000, // first byte, with start bit
					(byte) ((byte) channel << 6), (byte) 0b00000000 // third byte transmitted....don't care
			};
			if (channel > 3)
				data[0] = 0B00000111; // first Byte for Channel 3-7
			else
				data[0] = 0B00000110;// first Byte for Channel 0-3

			byte[] result = BMSys.spi2.write(data); // request data from MCP3208 via SPI

			int value = (result[1] << 8) & 0b0000111111111111; // merge data[1] & data[2] to get 10-bit result
			value |= (result[2] & 0xff);
			array[(channel + 8)] = value + oldValues[channel + 8];// fill the second half of the array
			// System.out.println(value); //test if analog number is necessary
		}
		return array;
	}

	// -----------------------------------------------------------------------------CalcAnalogData()--------------------->>
	public static double[] CalcAnalogData(int[] array) {
		double[] Values = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] DoubleArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		double[] CorVoltages = { 1, 0.995, 0.999, 1.006, 0.997, 0.995, 0.995, 0.995, 0.993, 0.998, 0.996, 0.995, 0.992,
				0.995, 1.005, 0.996 }; // these values correct the voltages for each battery
		double CorVoltFactor = 0.956;// you can correct all voltages with this factor (0.001 = ca. 0.01V)
		double prevVoltage = 0;// stores the previous voltage value
		double Value;// stores the voltage value temporary
		int BatCount = 0; // variable to count the number of battery per row
		for (int i = 0; i < 16; i++) {// correct the values with the factors
			DoubleArray[i] = (double) array[i] * CorVoltages[i] * CorVoltFactor;
		}
		for (int i = 0; i < Values.length; i++) {// fill in the calculated values
			if (BatCount < BatConfig.ConfDat[1]) {// sets prevVoltage to 0 if a new row starts
				BatCount++;
			} else {
				BatCount = 1;
				prevVoltage = 0;
			}
			Value = DoubleArray[i];
			Value = 129.5 / 4096 * Value; // calculate Number into Voltage
			Value = Value / 400; // division from CountBackgroundProcess
			Value = Value - prevVoltage; // subtract previous Voltage to get the Voltage of one specific Battery
			DecimalFormat df = new DecimalFormat("#.##");
			Value = Double.valueOf(df.format(Value));
			Values[i] = Value;
			prevVoltage = prevVoltage + Value;
		}
		return Values;
	}

	// -----------------------------------------------------------------------------SetConfig()-------------------------->>
	public static void SetConfig(int[] array) throws IOException {// stores the battery configuration to ConfDat.txt
		int sendx = array[0];
		int sendy = array[1];
		FileWriter fw = new FileWriter("/home/pi/Desktop/code/Data/ConfDat.txt");
		PrintWriter pw = new PrintWriter(fw);
		pw.print(sendx);
		pw.print(sendy);
		pw.close();
	}

	// -----------------------------------------------------------------------------GetConfig()-------------------------->>
	public static int[] GetConfig() throws IOException {// read the battery configuration from ConfDat.txt
		FileReader fr = new FileReader("/home/pi/Desktop/code/Data/ConfDat.txt");
		BufferedReader br = new BufferedReader(fr);
		int receivex = br.read();
		int receivey = br.read();
		receivex = receivex - 48;// convert ASCI to Number
		receivey = receivey - 48;// convert ASCI to Number
		br.close();
		int[] array = { 0, 0 };
		array[0] = receivex;
		array[1] = receivey;
		return array;
	}

	// -----------------------------------------------------------------------------SetChargeMode()--------------------->>
	public static boolean ChargeMode(boolean StatChargeMode) {// toggle charge mode and set the colors
		if (StatChargeMode == false) {// if status is inactive
			StatChargeMode = true;
			BMSys.panelChargeMode.setBackground(Color.MAGENTA);
			BMSys.lblChargeMode.setText("Modus EIN");
			for (int i = 0; i < 16; i++) {// test relays (turn all relays on):

				try {
					relays.get(i).low();
					Thread.sleep(20);// wait 20 ms until next relay turns on
					relays.get(i).high();
					Thread.sleep(20);// wait 20 ms until next relay turns on
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}

		} else {// if status is active
			StatChargeMode = false;
			BMSys.panelChargeMode.setBackground(Color.GREEN);
			BMSys.lblChargeMode.setText("Modus AUS");
			BMSys.lblTime.setText("");// make lblTime invisible
			for (int i = 0; i < 16; i++) {// turn all relays off:
				relays.get(i).high();
			}
		}
		return StatChargeMode;
	}

	// -----------------------------------------------------------------------------SetRelay()--------------------------->>
	public static int[] SetRelay(boolean StatChargeMode, double[] VoltageValues, int NumbOfBat) {
		double LowestVoltage = 1000;// stores the lowest battery voltage
		double max = 0.15;// gap how much higher a battery voltage needs to be to activate relay
		int[] StatusArray = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int maxBatAtLowVoltage = 0;
		if (StatChargeMode == true) {
			for (int i = 0; i < NumbOfBat; i++) {// Calculate lowest voltage
				if (VoltageValues[i] <= LowestVoltage) {
					LowestVoltage = VoltageValues[i];
				}
			}
			max = LowestVoltage + max;// if the voltage is above that value, the relay turns active
			for (int i = 0; i < NumbOfBat; i++) { // decide if relay should be active
				if (VoltageValues[i] >= max) {
					StatusArray[i] = 1;
					relays.get(i).low();

					try {
						Thread.sleep(20);// wait 20 ms until next relay turns on
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
					if (BatConfig.ConfDat[1] == 2) {//at 24V the voltage regulator can't handle more than 6 relays at once
						maxBatAtLowVoltage++;
						System.out.println("if wird ausgefuert");
						if (maxBatAtLowVoltage >= 6) {
							NumbOfBat=1;
							System.out.println("numbofbat = 1");
						}
					}
				} else {
					StatusArray[i] = 0;
					relays.get(i).high();
				}
			}
		} else {
			for (int i = 0; i < 16; i++) {// Set StatusArray to 0 (all relays get inactive)
				StatusArray[i] = 0;
			}
		}
		return StatusArray;
	}

	// -----------------------------------------------------------------------------TurnOffRelay()--------------------------->>
	public static void TurnOffRelay() {
		for (int i = 0; i < 16; i++) {// turn all relays inactive:
			relays.get(i).high();

		}
	}
}
