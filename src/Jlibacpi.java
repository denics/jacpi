import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
/**
 * This is the engine of the application. We get the infos from the acpi proc director. 
 * @author Denis Pitzalis 2003
 *
 */
public class Jlibacpi {
	//	Battery path
	private final String ACPI_BATTERY= "battery/";
	// Thermal path
	private final String ACPI_THERMAL= "thermal_zone/";
	//	Adapter path
	private final String ACPI_ADAPTER= "ac_adapter/";
	//	Fan path
	private final String ACPI_FAN= "fan/";
	private final String ACPI_PATH;
	private Vector batteryState;
	private Vector batteryInfo;
	public Jlibacpi(String CLIENT_VERSION, String acpi_path) {
		ACPI_PATH= acpi_path;
		System.out.print("ACPI server version... ");
		int SERVER_VERSION= getServerVersion();
		System.out.println(SERVER_VERSION);
		if (SERVER_VERSION < 20030813) {
			System.out.println("Ops... This client works only with server 20030813 or newest.");
			System.exit(0);
		}
		batteryState= new Vector();
	}
	/*
	 * Private Methods
	 */
	private void getBattState(String subPath) {
		batteryState= getRequestedInfo("battery", subPath, "state");
	}
	private void getBattInfo(String subPath) {
		batteryInfo= getRequestedInfo("battery", subPath, "info");
	}
	private Vector readData(String infoFile) {
		Vector results= new Vector();
		try {
			FileReader fr= new FileReader(infoFile);
			BufferedReader br= new BufferedReader(fr);
			String line= null;
			while ((line= br.readLine()) != null) {
				String namefield= new String();
				String result= new String();
				int separator= line.indexOf(':');
				results.add(line.substring(0, separator));
				results.add(line.substring(separator + 1));
			}
			fr.close();
		} catch (IOException e) {
			results= null;
		}
		//System.out.println(results.toString());
		return results;
	}
	/*
	 * Public Methods
	 */
	/**
	 * The client works only with version better then 20030813
	 * @return the version of the running Server
	 */
	public int getServerVersion() {
			Vector process= getRequestedInfo("server", "", "info");
			int version= 0;
			if (process != null) {
				version= Integer.parseInt(process.elementAt(process.indexOf("version") + 1).toString().replaceAll(" ", ""));
			}
			return version;
		}
	/**
	 * @param mainPath The main class we need (battery, fan, thermal_zone, ac_adapter, server)
	 * @param subPath In case of multiple instances of the object (BAT1, BAT2, etc)
	 * @param file The kind of info we need (state, info)
	 * @return all the line of the requested file
	 */
	public Vector getRequestedInfo(String mainPath, String subPath, String file) {
		String infoFile= null;
		if (mainPath.equalsIgnoreCase("server")) {
			infoFile= ACPI_PATH + file;
		} else if (mainPath.equalsIgnoreCase("ac_adapter")) {
			infoFile= ACPI_PATH + ACPI_ADAPTER + subPath + "/" + file;
		} else if (mainPath.equalsIgnoreCase("battery")) {
			infoFile= ACPI_PATH + ACPI_BATTERY + subPath + "/" + file;
		} else if (mainPath.equalsIgnoreCase("thermal")) {
			infoFile= ACPI_PATH + ACPI_THERMAL + subPath + "/" + file;
		} else if (mainPath.equalsIgnoreCase("fan")) {
			infoFile= ACPI_PATH + ACPI_FAN + subPath + "/" + file;
		}
		return readData(infoFile);
	}
	/**
	 * @param requested_dir class we want scan
	 * @return a list of subdirectory
	 */
	public String[] getList(String requested_dir) {
		String[] list= null;
		if (requested_dir.equals("thermal_zone")) {
			list= new File(ACPI_PATH + ACPI_THERMAL).list();
		} else if (requested_dir.equals("battery")) {
			list= new File(ACPI_PATH + ACPI_BATTERY).list();
		} else if (requested_dir.equals("ac_adapter")) {
			list= new File(ACPI_PATH + ACPI_ADAPTER).list();
		} else if (requested_dir.equals("fan")) {
			list= new File(ACPI_PATH + ACPI_FAN).list();
		}
		return list;
	}
	/**
	 * TODO I've no fan to do testing
	 * @param subPath (FAN1, FAN2, etc.)
	 * @return at the moment nothing
	 */
	public String getFanState(String subPath) {
		return null;
	}
	/**
	 * @param subPath (THRM1, THRM2, etc.)
	 * @return the temperature in our computer 
	 */
	public String getThermState(String subPath) {
		Vector process= getRequestedInfo("thermal", subPath, "temperature");
		String tmp= null;
		if (process != null) {
			tmp= process.elementAt(process.indexOf("temperature") + 1).toString().replaceAll(" ", "");
		}
		return tmp;
	}
	/**
	 * @param subPath (ACAD1, ACAD2, etc.)
	 * @return the state of the ac adapter
	 */
	public String getACState(String subPath) {
		Vector process= getRequestedInfo("ac_adapter", subPath, "state");
		String state= null;
		if (process != null) {
			state= process.elementAt(process.indexOf("state") + 1).toString().replaceAll(" ", "");
		}
		return state;
	}
	/**
	 * @param subPath (BAT1, BAT2, etc.)
	 * @return if the battery is present or not
	 */
	public String getBattPres(String subPath) {
		getBattState(subPath);
		String present= "no";
		if (batteryState != null) {
			present= batteryState.elementAt(batteryState.indexOf("present") + 1).toString().replaceAll(" ", "");
		}
		return present;
	}
	/**
	 * @param subPath (BAT1, BAT2, etc.)
	 * @return the perc of charge available
	 */
	public int getBattPerc(String subPath) {
		getBattInfo(subPath);
		String tmp= null;
		int CurrBarValue= 0;
		int MaxBarValue= 0;
		int result= 0;
		tmp= batteryInfo.elementAt(batteryInfo.indexOf("last full capacity") + 1).toString().replaceAll(" ", "").replaceAll("mWh", "");
		if (tmp != null)
			MaxBarValue= Integer.parseInt(tmp);
		tmp= batteryState.elementAt(batteryState.indexOf("remaining capacity") + 1).toString().replaceAll(" ", "").replaceAll("mWh", "");
		if (tmp != null)
			CurrBarValue= Integer.parseInt(tmp);
		try {
			result= CurrBarValue * 100 / MaxBarValue;
		} catch (ArithmeticException e) {
			result= 0;
		}
		return result;
	}
	/**
	 * @param subPath (BAT1, BAT2, etc.)
	 * @return all the info from the battery manufacturer
	 */
	public String[] getBatteryInfo(String subPath) {
		getBattInfo(subPath);
		String[] present= null;
		if (batteryInfo != null) {
			present= new String[batteryInfo.size()];
			System.arraycopy(batteryInfo.toArray(),0,present,0, batteryInfo.size());
		}
		return present;
	}
}
