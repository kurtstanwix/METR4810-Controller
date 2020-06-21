import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

import com.intel.bluetooth.BluetoothConsts;

public class ControllerDiscoveryListener implements DiscoveryListener {
	// Internal mapping of friendly Bluetooth names to their address
	private Map<String, RemoteDevice> foundDevices = new HashMap<>();
	// Internal list of the services offered on devices
	private List<ServiceRecord> foundServices = new ArrayList<>();;
	// Status of a device inquiry
	public int discoveryStatus = DiscoveryListener.INQUIRY_COMPLETED;
	// Status of a service inquiry
	public int serviceStatus = DiscoveryListener.SERVICE_SEARCH_TERMINATED;

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		try {
			// A device was found, try to get its friendly name
			String name = btDevice.getFriendlyName(true);
			synchronized (this) {
				foundDevices.put(name, btDevice);
			}
		} catch (IOException io) {
			// Can't get device name, add the address instead
			synchronized (this) {
				foundDevices.put(btDevice.getBluetoothAddress(), btDevice);
			}
		}
	}

	@Override
	public void inquiryCompleted(int discType) {
		Controller.send("Completed Search\r\n", false);
		discoveryStatus = discType;
		synchronized (this) {
			// Let any functions waiting on this know the inquiry is done
			notifyAll();
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		serviceStatus = respCode;
		synchronized (this) {
			// Let any functions waiting on this know the search is done
			notifyAll();
		}
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		synchronized (this) {
			// Found services, loop through and add any serial port ones
			for (int i = 0; i < servRecord.length; i++) {
				Enumeration<?> e = (Enumeration<?>) servRecord[i].getAttributeValue(BluetoothConsts.ServiceClassIDList)
						.getValue();
				if (BluetoothConsts.SERIAL_PORT_UUID.equals(((DataElement) e.nextElement()).getValue())) {
					foundServices.add(servRecord[i]);
				}
			}
		}
	}

	/**
	 * Clear the list of previously found devices
	 */
	public void clearFoundDevices() {
		foundDevices.clear();
	}

	/**
	 * Get the mapping of previously found devices
	 * 
	 * @return mapping of previously found devices
	 */
	public synchronized Map<String, RemoteDevice> getFoundDevices() {
		return new HashMap<String, RemoteDevice>(foundDevices);
	}

	/**
	 * Clear the list of previously found services
	 */
	public void clearFoundServices() {
		foundServices.clear();
	}

	/**
	 * Get the list of previously found services
	 * 
	 * @return list of previously found services
	 */
	public synchronized List<ServiceRecord> getFoundServices() {
		return new ArrayList<ServiceRecord>(foundServices);
	}

}
