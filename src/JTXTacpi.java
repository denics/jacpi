import java.text.SimpleDateFormat;
/**
 * Not nice but practical shell interface
 * @author Denis Pitzalis 2003
 *
 */
public class JTXTacpi {
	private volatile Thread timer;
	private SimpleDateFormat formatter;
	private Jlibacpi la;
	private String[] Adapt;
	private String[] Therm;
	private String[] Batt;
	private String[] Fan;
	public JTXTacpi(String client_version, String acpi_path, String arg) {
		String CLIENT_VERSION= client_version;
		String ACPI_PATH= acpi_path;
		//
		la= new Jlibacpi(CLIENT_VERSION, ACPI_PATH);
		Therm= getList("thermal_zone");
		Batt= getList("battery");
		Adapt= getList("ac_adapter");
		Fan= getList("fan");
		if (arg != "0") {
			boolean ok= false;
			System.out.println("Seems you ask for information... well");
			for (int i= 0; i < Batt.length; i++)
				if (arg.equalsIgnoreCase(Batt[i]))
					ok= true;
			if (ok) {
				String[] info= getBattInfo(arg);
				for (int y= 0; y < info.length - 1; y += 2) {
					System.out.print("## " + arg + " --> " + info[y] + ":" + info[y + 1] + "\n");
				}
			} else {
				if (arg != "0")
					System.out.println("Sorry, your request is not supported.");
			}
		}
		for (int i= 0; i < Adapt.length; i++) {
			System.out.println("AC-adapter " + Adapt[i] + " state: " + getACState(Adapt[i]));
		}
		for (int i= (Batt.length - 1); i >= 0; i--) {
			String batt_state= getBattPres(Batt[i]);
			if (batt_state.equals("yes")) {
				System.out.println("Battery " + Batt[i] + ": " + getBattPerc(Batt[i]) + "% ");
			} else {
				System.out.println("Battery " + Batt[i] + ": not present");
			}
		}
		for (int i= 0; i < Therm.length; i++) {
			System.out.println("Thermal " + Therm[i] + " temperature: " + getThermState(Therm[i]));
		}
		for (int i= 0; i < Fan.length; i++) {
			System.out.println("Fan " + Fan[i] + " state: " + getFanState(Fan[i]));
		}
	}
	private String[] getList(String request) {
		return la.getList(request);
	}
	private String getACState(String subPath) {
		return la.getACState(subPath);
	}
	private String getFanState(String subPath) {
		return la.getFanState(subPath);
	}
	private String getBattPres(String subPath) {
		return la.getBattPres(subPath);
	}
	private int getBattPerc(String subPath) {
		return la.getBattPerc(subPath);
	}
	private String[] getBattInfo(String subPath) {
		return la.getBatteryInfo(subPath);
	}
	private String getThermState(String subPath) {
		return la.getThermState(subPath);
	}
}
