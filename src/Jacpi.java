import java.io.File;
/**
 * The java client is a simple application that connect to the acpid interface and retrive the info about the system. 
 * @author Denis Pitzalis 2004
 * @version 0.04
 */
public class Jacpi {
	private static String ACPI_PATH= "/proc/acpi/";
	private static final String CLIENT_VERSION= "0.04";
	public static void main(String[] args) {
		if (System.getProperty("java.runtime.version").substring(0, 5).equalsIgnoreCase("1.4.2")) {
			File f= new File(ACPI_PATH);
			System.out.println("Denis Pitzalis 2004");
			System.out.println("Jacpi " + CLIENT_VERSION);
			System.out.print("Let me see if you have acpi support enabled... ");
			if (f.exists()) {
				System.out.println("Ok");
				System.out.print("X support... ");
				try {
					JGUIacpi gui= new JGUIacpi(CLIENT_VERSION, ACPI_PATH);
				} catch (InternalError e) {
					System.out.println("No");
					System.out.println("Warning: no X support. Starting in text mode");
					String arg= "0";
					if (args.length > 0)
						arg= args[0];
					JTXTacpi la= new JTXTacpi(CLIENT_VERSION, ACPI_PATH, arg);
				}
			} else {
				System.out.println("No");
				System.out.println("ACPI is not configured on your computer!");
				System.out.println("Bye Bye");
			}
		} else {
			System.out.println("This version of Jacpi works only with java 1.4.2!");
		}
		System.out.println("http://jacpi.sf.net");
	} //main
}
