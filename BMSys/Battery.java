import java.awt.Color;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Battery extends JLabel {

	JLabel lblBat = new JLabel("");
	JPanel pnlBat = new JPanel();
	JLabel lblText = new JLabel("Suche...");
	private static final long serialVersionUID = 1L;

	public Battery() {// constructor

		this.setBounds(20, 60, 90, 50);
		this.setLayout(null);
		pnlBat.setBounds(5, 11, 73, 28);
		this.add(pnlBat);
		pnlBat.setBackground(Color.GREEN);
		lblText.setForeground(Color.BLACK);
		pnlBat.add(lblText);
		lblBat.setBounds(0, 0, 90, 50);
		this.add(lblBat);
		this.setIcon(new ImageIcon("/home/pi/Desktop/code/Data/battery.jpg"));
	}
}
