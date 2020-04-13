import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.intel.bluetooth.BluetoothConsts;

public class ControllerBT {

	// Class UUID for bt serial ports
	private static final int SERIAL_PORT_CLASS = 0x1101;
	// Bluetooth profile service name for serial port profile
	private static final int SERIAL_PORT_PROFILE = 0x0100;
	boolean scanFinished = false;

	// boolean scanFinished = false;
	// private RemoteDevice hc05device = null;;
	private LocalDevice controllerBT;
	// final Object deviceLock = new Object();
	String hc05Url;

	private ControllerDiscoveryListener listener = new ControllerDiscoveryListener();

	// BT device name (or address if name can't be obtained) and device pairs
	Map<String, RemoteDevice> foundDevices;

	// public static void main(String[] args) {
	// try {
	// new HC05().go();
	// } catch (Exception ex) {
	// Logger.getLogger(HC05.class.getName()).log(Level.SEVERE, null, ex);
	// }
	// }

	public ControllerBT(LocalDevice controllerBT) {
		this.controllerBT = controllerBT;
	}

	public ControllerBT() throws BluetoothStateException {
		this(LocalDevice.getLocalDevice());
	}

	public List<String> findDevices() throws InterruptedException {
		// scan for all devices:
		// System.out.println(LocalDevice.getProperty(BlueCoveLocalDeviceProperties.LOCAL_DEVICE_PROPERTY_STACK));

		System.out.println("Searching for devices...");
		synchronized (listener) {
			listener.clearFoundDevices();
			try {
				controllerBT.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
			} catch (BluetoothStateException e) {
				// Couldn't start the inquiry
				e.printStackTrace();
				return null;
			}
			listener.wait();
		}
		foundDevices = listener.getFoundDevices();
		return new ArrayList<>(foundDevices.keySet());
	}

	// Must be called after findDevices
	// deviceName must be a name from the list returned by foundDevices
	public StreamConnection connect(String deviceName) throws InterruptedException {

		RemoteDevice device = foundDevices.get(deviceName);
		if (device == null) {
			return null;
		}
		// search for services:
		// UUID uuid = new UUID(0x1101); //scan for btspp://... services (as HC-05
		// offers it)
		UUID[] searchUUIDSet = new UUID[] { BluetoothConsts.SERIAL_PORT_UUID };
		int[] attrIDs = new int[] { SERIAL_PORT_PROFILE }; // service name

		scanFinished = false;
		synchronized (listener) {
			//listener.clearFoundServices();
			try {
				controllerBT.getDiscoveryAgent().searchServices(attrIDs, searchUUIDSet, device, listener);
			} catch (BluetoothStateException e2) {
				e2.printStackTrace();
				return null;
			}
			listener.wait();
		}
		if (listener.getFoundServices().size() == 0) {
			return null;
		}
		// Connect to the first service found
		StreamConnection result = null;
		try {
			// Connect to the first service found
			result = (StreamConnection) Connector.open(listener.getFoundServices().get(0).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	/*
	 * while (!scanFinished) { try { Thread.sleep(500); } catch
	 * (InterruptedException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 * 
	 * System.out.println("Connected to ADDR: " + device.getBluetoothAddress());
	 * System.out.println(hc05Url);
	 * 
	 * // if you know your hc05Url this is all you need: StreamConnection
	 * streamConnection = null; OutputStream os = null; InputStream is = null; try {
	 * streamConnection = (StreamConnection) Connector.open(hc05Url); os =
	 * streamConnection.openOutputStream(); is = streamConnection.openInputStream();
	 * } catch (IOException e2) { // TODO Auto-generated catch block
	 * e2.printStackTrace(); }
	 * 
	 * boolean receiving = false; byte[] btBuffer = new byte[1024]; byte[] inBuffer
	 * = new byte[1024]; InputStream userInput = System.in; int readByte = -1; int
	 * btPos = 0; int inPos = 0; int mode = 0; String input; while (true) { if (mode
	 * == 0) {
	 * 
	 * 
	 * } else if (mode == 1) { input = "+STATE:CONNECTED\r\n"; try {
	 * os.write(input.getBytes()); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } mode = 0; } }
	 * System.out.println("OUT OF HERE"); // } // is. try { os.close(); is.close();
	 * streamConnection.close(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); } }
	 */
}