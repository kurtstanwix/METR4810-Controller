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
import com.intel.bluetooth.RemoteDeviceHelper;

public class ControllerBT {

	// Class UUID for bt serial ports
	private static final int SERIAL_PORT_CLASS = 0x1101;
	// Bluetooth profile service name for serial port profile
	private static final int SERIAL_PORT_PROFILE = 0x0100;
	// Flag to indicate when a scan is finished
	boolean scanFinished = false;

	// The local Bluetooth adaptor
	private LocalDevice localBTDevice;

	// Passcode to connect to Bluetooth devices
	private static final String PASSCODE = "1234";

	// Listener service to find Bluetooth devices and services
	private ControllerDiscoveryListener listener = new ControllerDiscoveryListener();

	// BT device name (or address if name can't be obtained) and device pairs
	private Map<String, RemoteDevice> foundDevices;

	/**
	 * Setup the Bluetooth controller
	 * 
	 * @param BTDevice
	 *            local BLuetooth device to use for connections.
	 */
	public ControllerBT(LocalDevice BTDevice) {
		this.localBTDevice = BTDevice;
	}

	/**
	 * Setup the Bluetooth controller with the API's chosen local device.
	 * 
	 * @throws BluetoothStateException
	 */
	public ControllerBT() throws BluetoothStateException {
		this(LocalDevice.getLocalDevice());
	}

	/**
	 * Finds the list of nearby Bluetooth devices with their friendly names
	 * 
	 * @return List of the friendly names of nearby devices, or their address if
	 *         friendly name could not be retrieved.
	 * @throws InterruptedException
	 */
	public List<String> findDevices() throws InterruptedException {
		Controller.send("Searching for devices...", false);
		// Syncronised to be interrupted by the listener when complete
		synchronized (listener) {
			listener.clearFoundDevices(); // Forget previous searches
			/*
			 * RemoteDevice preknown[] =
			 * this.localBTDevice.getDiscoveryAgent().retrieveDevices(DiscoveryAgent.
			 * PREKNOWN); if (preknown != null) { for (RemoteDevice dev : preknown) { try {
			 * RemoteDeviceHelper.removeAuthentication(dev); } catch (IOException e) { //
			 * May have already cleared } } }
			 */
			try {
				localBTDevice.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
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

	/**
	 * Connects to device found by findDevices. Call this after findDevices.
	 * 
	 * @param deviceName
	 *            name of the device to connect to, must be in list returned by
	 *            findDevices
	 * @return a StreamConnection to the Bluetooth device, null if could not connect
	 * @throws InterruptedException
	 */
	public StreamConnection connect(String deviceName) throws InterruptedException {
		RemoteDevice device = foundDevices.get(deviceName);
		if (device == null) {
			// Name was not in found list
			return null;
		}
		// Scan for "btspp://..." services as HC-05 offers it
		UUID[] searchUUIDSet = new UUID[] { BluetoothConsts.SERIAL_PORT_UUID };
		int[] attrIDs = new int[] { SERIAL_PORT_PROFILE }; // Service name

		scanFinished = false;
		// Syncronised to be interrupted by the listener when complete
		synchronized (listener) {
			listener.clearFoundServices();
			try {
				localBTDevice.getDiscoveryAgent().searchServices(attrIDs, searchUUIDSet, device, listener);
			} catch (BluetoothStateException e2) {
				e2.printStackTrace();
				return null;
			}
			listener.wait();
		}
		if (listener.getFoundServices().size() == 0) {
			return null; // No devices with the correct service offered
		}
		StreamConnection result = null;
		try {
			// Automatically authenticate
			RemoteDeviceHelper.authenticate(device, PASSCODE);
			// Connect to the first service found (only one on HC-05)
			result = (StreamConnection) Connector.open(
					listener.getFoundServices().get(0).getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}