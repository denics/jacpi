import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
/**
 * Beautiful GUI frontend to the library, usable only if we use X. We can also have a beautiful icon in our space, like an applet ;)
 * @author Denis Pitzalis 2003
 *
 */
public class JGUIacpi extends JFrame implements Runnable {
	private final Icon nobattery= new ImageIcon("images/nobattery.png");
	private final Icon battery= new ImageIcon("images/battery.png");
	private final Icon charging= new ImageIcon("images/charging.png");
	private final Image jacpi= createIcon("images/jacpi.png");
	private final Icon online= new ImageIcon("images/on-line.png");
	private Thread timer;
	private Jlibacpi la;
	private String[] Adapt;
	private JLabel ac_state;
	private String[] Batt;
	private JButton[] battPres;
	private JProgressBar[] battProg;
	private long time= 100;
	private JProgressBar battTotProg;
	private JLabel[] thermPres;
	private String[] Therm;
	private String[] Fan;
	private String CLIENT_VERSION;
	private JFrame acpiIcon;
	private JButton iconb;
	public JGUIacpi(String client_version, String acpi_path) {
		CLIENT_VERSION= client_version;
		String ACPI_PATH= acpi_path;
		System.out.println("Ok");
		// GUI init
		pack();
		acpiIcon= new JFrame();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setResizable(false);
		setIconImage(jacpi);
		setTitle("Jacpi " + CLIENT_VERSION);
		getContentPane().setLayout(new BorderLayout());
		JPanel frame= new JPanel();
		timer= new Thread(this);
		timer.start();
		// init
		la= new Jlibacpi(CLIENT_VERSION, ACPI_PATH);
		Therm= getList("thermal_zone");
		int NumTherm= Therm.length;
		// for users with laptops that use external batteries (ex: Asus D1 series) or have no
		// batteries currently in their laptop
		// Thank's to Jennifer Pinkham <jennifer@unrulygrrl.org>
		int NumBatt;
		try {
			Batt= getList("battery");
			NumBatt= Batt.length;
		} catch(NullPointerException ne){
			NumBatt = 0; 
		}
		Adapt= getList("ac_adapter");
		int NumAdapt= Adapt.length;
		Fan= getList("fan");
		// if the fans cannot be detected
		// Thank's to Jennifer Pinkham <jennifer@unrulygrrl.org>
		int NumFan;
		try {
			Fan= getList("fan");
			NumFan= Fan.length;
		} catch (NullPointerException ne){
			NumFan = 0;
		}
		//
		// menu
		JMenuBar menuBar= new JMenuBar();
		JMenu menumisc= new JMenu("Misc");
		JMenuItem iconify= new JMenuItem("Iconify");
		iconify.setActionCommand("Iconify");
		iconify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openInfo(e.getActionCommand());
			}
		});
		menumisc.add(iconify);
		menumisc.add(new JSeparator());
		JMenuItem exit= new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		menumisc.add(exit);
		menuBar.add(menumisc);
		menuBar.add(Box.createHorizontalGlue());
		JMenu menuhelp= new JMenu("Help");
		JMenuItem about= new JMenuItem("About");
		about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(
					new JFrame(),
					"<html><h2><center><font color=#FFFF00> Java ACPI client "
						+ CLIENT_VERSION
						+ "</center></h2><h3><center>Jacpi <a href='http://jacpi.sf.net/'>http://jacpi.sf.net/</a></center></h3><p>The java client is a simple application that connect to the acpid interface <br> and retrive the info about the system. </p><font color=#000000 size=4> <hr><p>&#169;2003 Denis Pitzalis - denics@free.fr</p>",
					"Jacpi :: About",
					JOptionPane.INFORMATION_MESSAGE,
					new ImageIcon(jacpi));
			}
		});
		menuhelp.add(about);
		menuBar.add(menuhelp);
		//
		getContentPane().add(BorderLayout.NORTH, menuBar);
		setSize(300, (80 * (NumTherm + NumBatt + NumAdapt + NumFan)));
		frame.setLayout(new GridLayout((NumBatt + NumAdapt + NumTherm + NumFan), 1));
		//
		if (NumAdapt > 0) {
			for (int i= 0; i < NumAdapt; i++) {
				JPanel ac= new JPanel();
				ac.setLayout(new GridLayout(1, 2));
				ac.setBorder(BorderFactory.createTitledBorder("Adapter Load Information: "));
				ac_state= new JLabel();
				ac_state.setText("State: " + getACState(Adapt[i]));
				battTotProg= new JProgressBar();
				battTotProg.setVisible(false);
				ac.add(ac_state);
				ac.add(battTotProg);
				frame.add(ac);
			}
		}
		//
		if (NumBatt > 0) {
			battPres= new JButton[NumBatt];
			battProg= new JProgressBar[NumBatt];
			for (int i= (NumBatt - 1); i >= 0; i--) {
				JPanel batt= new JPanel();
				batt.setLayout(new GridLayout(1, 2));
				batt.setBorder(BorderFactory.createTitledBorder(Batt[i] + " information: "));
				battPres[i]= new JButton();
				battPres[i].setBorder(null);
				battPres[i].setActionCommand(Batt[i]);
				battPres[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String[] info= getBattInfo(e.getActionCommand());
						StringBuffer sb= new StringBuffer();
						for (int y= 0; y < info.length - 1; y += 2) {
							sb.append("<html><font size=3>" +info[y] + ": " + info[y + 1] + "\n");
						}
						JOptionPane.showMessageDialog(new JFrame(), sb.toString(), "Jacpi :: " + e.getActionCommand() + " generic info", JOptionPane.INFORMATION_MESSAGE);
					}
				});
				battProg[i]= new JProgressBar();
				batt.add(battPres[i]);
				batt.add(battProg[i]);
				frame.add(batt);
			}
		}
		if (NumTherm > 0) {
			for (int i= 0; i < NumTherm; i++) {
				thermPres= new JLabel[NumTherm];
				JPanel therm= new JPanel();
				therm.setBorder(BorderFactory.createTitledBorder(Therm[i] + " information: "));
				thermPres[i]= new JLabel();
				therm.add(thermPres[i]);
				frame.add(therm);
			}
		}
		if (NumFan > 0) {
			for (int i= 0; i < NumFan; i++) {
				JPanel fan= new JPanel();
				//fan.setLayout(new GridLayout(2, 1));
				fan.setBorder(BorderFactory.createTitledBorder(Fan[i] + " information: "));
				frame.add(fan);
			}
		}
		getContentPane().add(frame);
		show();
	}
	protected void openInfo(String info) {
		this.setVisible(false);
		acpiIcon.setVisible(true);
		acpiIcon.setIconImage(jacpi);
		acpiIcon.setSize(64, 64);
		iconb= new JButton();
		iconb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				acpiIcon.setVisible(false);
				setVisible(true);
			}
		});
		acpiIcon.getContentPane().add(iconb);
		acpiIcon.setResizable(false);
		acpiIcon.setTitle("Jacpi :: " + CLIENT_VERSION);
		acpiIcon.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		acpiIcon.show();
	}
	/*
	 * GET/SET Method
	 */
	protected String[] getBattInfo(String subPath) {
		return la.getBatteryInfo(subPath);
	}
	private String getBattPres(String subPath) {
		return la.getBattPres(subPath);
	}
	private int getBattPerc(String subPath) {
		return la.getBattPerc(subPath);
	}
	private String getACState(String subPath) {
		return la.getACState(subPath);
	}
	private String getThermState(String subPath) {
		return la.getThermState(subPath);
	}
	private String getFanState(String subPath) {
		return la.getFanState(subPath);
	}
	private String[] getList(String request) {
		return la.getList(request);
	}
	private Image createIcon(String path) {
		int MAX_IMAGE_SIZE= 75000;
		int count= 0;
		BufferedInputStream imgStream= new BufferedInputStream(Jacpi.class.getResourceAsStream(path));
		if (imgStream != null) {
			byte buf[]= new byte[MAX_IMAGE_SIZE];
			try {
				count= imgStream.read(buf);
			} catch (IOException ieo) {
				System.out.println("Couldn't read stream from file: " + path);
			}
			try {
				imgStream.close();
			} catch (IOException ieo) {
				System.out.println("Can't close file " + path);
			}
			if (count <= 0) {
				System.out.println("Empty file: " + path);
				return null;
			}
			return Toolkit.getDefaultToolkit().createImage(buf);
		} else {
			System.out.println("Couldn't find file: " + path);
			return null;
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		Thread me= Thread.currentThread();
		while (timer == me) {
			try {
				Thread.sleep(time);
				time= 10000;
			} catch (InterruptedException e) {
			}
			try{
			for (int i= 0; i < Adapt.length; i++) {
				ac_state.setText("State: " + getACState(Adapt[i]));
			}
			for (int i= 0; i < Therm.length; i++) {
				thermPres[i].setText(getThermState(Therm[i]));
			}
			int TotProg= 0;
			String batt_state= null;
			for (int i= (Batt.length - 1); i >= 0; i--) {
				batt_state= getBattPres(Batt[i]);
				if (batt_state.equals("yes")) {
					battProg[i].setVisible(true);
					if (ac_state.getText().equalsIgnoreCase("State: on-line")) {
						battPres[i].setIcon(charging);
						battTotProg.setVisible(false);
					} else {
						battPres[i].setIcon(battery);
						battTotProg.setVisible(true);
					}
					int batt_perc= getBattPerc(Batt[i]);
					if (batt_perc > 30) {
						battProg[i].setForeground(Color.GREEN);
					} else if (batt_perc > 10) {
						battProg[i].setForeground(Color.YELLOW);
					} else {
						battProg[i].setForeground(Color.RED);
					}
					battProg[i].setValue(batt_perc);
					battProg[i].setToolTipText(batt_perc + "%");
					TotProg= TotProg + battProg[i].getValue();
				} else {
					battProg[i].setVisible(false);
					battPres[i].setIcon(nobattery);
				}
				battTotProg.setValue(TotProg);
				battTotProg.setToolTipText(TotProg + "%");
			}
			if (acpiIcon.isVisible()) {
				if (Batt.length > 0) {
					boolean Battery= false;
					if (ac_state.getText().equalsIgnoreCase("State: on-line")) {
						for (int i = 0; i < Batt.length; i++)
							if (getBattPres(Batt[i]) == "yes")
								Battery= true;
						if (Battery) {
							iconb.setIcon(charging);
						} else {
							iconb.setIcon(online);
							TotProg= 100;
						}
					} else {
						iconb.setIcon(battery);
					}
				} else {
					iconb.setIcon(nobattery);
				}
				if (TotProg > 30) {
					iconb.setBackground(Color.GREEN);
				} else if (TotProg > 10) {
					iconb.setBackground(Color.YELLOW);
				} else {
					iconb.setBackground(Color.RED);
				}
				iconb.setToolTipText(TotProg + "%");
			}
			} catch(NullPointerException n){
			}
		}
	}
}
