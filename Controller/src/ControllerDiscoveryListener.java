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

	private Map<String, RemoteDevice> foundDevices = new HashMap<>();
	private List<ServiceRecord> foundServices = new ArrayList<>();;
	public int discoveryStatus = DiscoveryListener.INQUIRY_COMPLETED;
	public int serviceStatus = DiscoveryListener.SERVICE_SEARCH_TERMINATED;

	@Override
	public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
		try {
			String name = btDevice.getFriendlyName(true);
			synchronized (this) {
				foundDevices.put(name, btDevice);
			}
		} catch (IOException io) {
			// Can't get device name
			synchronized (this) {
				foundDevices.put(btDevice.getBluetoothAddress(), btDevice);
			}
		}
	}

	@Override
	public void inquiryCompleted(int discType) {
		System.out.println("Completed Search");
		discoveryStatus = discType;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public void serviceSearchCompleted(int transID, int respCode) {
		System.out.println("Completed Search");
		serviceStatus = respCode;
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
		// System.out.println(servRecord.length);
		synchronized (this) {
			for (int i = 0; i < servRecord.length; i++) {
				Enumeration<?> e=(Enumeration<?>) servRecord[i].getAttributeValue(BluetoothConsts.ServiceClassIDList).getValue();
				if (BluetoothConsts.SERIAL_PORT_UUID.equals(((DataElement) e.nextElement()).getValue())) {
					foundServices.add(servRecord[i]);
				}
				
				//String hc05Url = servRecord[i].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
				//System.out.println(hc05Url);
				//for (int j = 0; j < servRecord[i].getAttributeIDs().length; j++) {
				//	int id = servRecord[i].getAttributeIDs()[j];
				//	System.out.println("" + j + ": " + id + " - " + servRecord[i].getAttributeValue(id).toString());
				//}
				// if (hc05Url != null) {
				// break; //take the first one
				// }
			}
		}
	}

	public void clearFoundDevices() {
		foundDevices.clear();
	}
	
	public synchronized Map<String, RemoteDevice> getFoundDevices() {
		return new HashMap<String, RemoteDevice>(foundDevices);
	}
	
	public void clearFoundServices() {
		foundServices.clear();
	}
	
	public synchronized List<ServiceRecord> getFoundServices() {
		return new ArrayList<ServiceRecord>(foundServices);
	}

}
