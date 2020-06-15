import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import com.intel.bluetooth.BlueCoveLocalDeviceProperties;

public class Controller {

	private static final int INQUIRY_ATTEMPTS = 3;
	
	
	public static IOConnection debug;

	public static void main(String[] args) {
		
		debug = new IOConnection("Debug", new ConsoleConnection());
		if (!debug.isClosed()) {
			new Thread(debug).start();
		}
		
		LocalDevice controllerBTDevice = null;
		try {
			controllerBTDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			//e.printStackTrace();
			debug.send("No Bluetooth host device detected.\r\n");
			System.exit(1);
		}
		// Set how long the device spends enquiring for other devices
		BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_INQUIRY_DURATION, "3");
		
		//debug.send("Local Address: " + controllerBTDevice.getBluetoothAddress() + "\r\n");
		ControllerBT managerBT = new ControllerBT(controllerBTDevice);
		
		// Find the BT devices in the area
		List<String> foundDevices = null;
		int scanAttempts = 0;
		for (; scanAttempts < INQUIRY_ATTEMPTS; scanAttempts++) {
			try {
				foundDevices = managerBT.findDevices();
			} catch (InterruptedException e) {
				// Shouldn't get here, nothing interrupts findDevices
				e.printStackTrace();
			}
			if (foundDevices != null && !foundDevices.isEmpty()) {
				break; // Found some devices
			}
		}
		if (scanAttempts == INQUIRY_ATTEMPTS) {
			System.err.println("Scan attempts yielded no devices.");
			System.exit(1);
		}

		// List out the found devices
		debug.send("Found Devices:\r\n");
		for (String device : foundDevices) {
			debug.send(String.format("  Name: \"%s\"\r\n", device));
		}
		
		/*
		// User selects desired BT device
		System.out.print("Enter Bluetooth Device Name: ");
		String name;

		while (true) {
			if (debug.hasInput()) {
				name = debug.getInput();
				if (foundDevices.contains(name = name.replace("\n", "").replace("\r", ""))) {
					break;
				}
				System.out.println("Bluetooth device name was not found");
				System.out.print("Enter Bluetooth Device Name: ");
			}
		}
		*/

		//CommsFSM transport = new Transport(manager);
		CommsFSM handler = new Handler(managerBT);
		CommsFSM transport = new Transport(managerBT);
		/*try {
			transport = new IOConnection("Transport", manager.connect(name));
			if (!transport.isClosed()) {
				new Thread(transport).start();
			}
		} catch (InterruptedException e) {
			// Shouldn't get here, nothing interrupts connect
			e.printStackTrace();
		}*/

		//System.out.println(""
		//		+ controllerBTDevice.getProperty(BlueCoveLocalDeviceProperties.LOCAL_DEVICE_DEVICES_LIST));
		while (true) {
			
			if (debug.hasInput()) {
				byte[] temp = debug.getInput().getBytes();
				byte[] toSend = new byte[temp.length];
				System.arraycopy(temp, 1, toSend, 1, temp.length - 1);
				if (temp[0] == 'T') {
					transport.send(toSend);
				} else if (temp[0] == 'H') {
					handler.send(toSend);
				}
			}
			if (debug.isClosed() || handler.isClosed() || transport.isClosed()) {
				System.err.println("Closing");
				break;
			}
			handler.process();
			transport.process();
		}
		handler.close();
		transport.close();
		debug.close();

		// RemoteDevice transportConnection = foundDevices.get(name);
		// manager.connect(transport);
		/*
		 * while (true) {
		 * 
		 * try { if (userInput.available() != 0) { readByte = userInput.read(); if
		 * (readByte == -1) { System.out.println("Bad Input"); break; } inBuffer[inPos]
		 * = (byte) readByte; if (inBuffer[inPos] == '\n') { if (inBuffer[inPos - 1] ==
		 * '\r') { String temp = new String(inBuffer, 0, inPos + 1);
		 * os.write(temp.getBytes()); inPos = 0; // mode = 1; } } else { inPos++; } } }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } }
		 */
		// userInput.close();
	}

}
