import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import com.intel.bluetooth.BlueCoveLocalDeviceProperties;

public class Controller {

	public static void main(String[] args) {

		LocalDevice controllerBTDevice = null;
		try {
			controllerBTDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			e.printStackTrace();
		}
		BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_INQUIRY_DURATION, "1");
		
		System.out.println("" + controllerBTDevice.getProperty(BlueCoveLocalDeviceProperties.LOCAL_DEVICE_DEVICES_LIST));
		
		System.out.println("Local Address: " + controllerBTDevice.getBluetoothAddress());
		ControllerBT manager = new ControllerBT(controllerBTDevice);

		// controllerBT.
		// Find the BT devices in the area
		List<String> foundDevices = null;
		try {
			foundDevices = manager.findDevices();
		} catch (InterruptedException e) {
			// Shouldn't get here, nothing interrupts findDevices
			e.printStackTrace();
		}

		// List out the found devices
		System.out.println("Found Devices:");
		for (String device : foundDevices) {
			System.out.format("  Name: \"%s\"\n", device);
		}
		// User selects desired BT device
		//InputStream userInput = System.in;
		//Scanner userScanner = new Scanner(userInput);

		System.out.print("Enter Bluetooth Device Name: ");
		String name;
		IOConnection console = new IOConnection("Console", new ConsoleConnection());
		if (!console.isClosed()) {
			new Thread(console).start();
		}
		
		while (true) {
			if (console.hasInput()) {
				name = console.getInput();
				if (foundDevices.contains(name = name.replace("\n", "").replace("\r", ""))) {
					break;
				}
				System.out.println("Bluetooth device name was not found");
				System.out.print("Enter Bluetooth Device Name: ");
			}
		}

		IOConnection transport = null;
		try {
			transport = new IOConnection("Transport", manager.connect(name));
			if (!transport.isClosed()) {
				new Thread(transport).start();
			}
		} catch (InterruptedException e) {
			// Shouldn't get here, nothing interrupts connect
			e.printStackTrace();
		}

		System.out.println("" + controllerBTDevice.getProperty(BlueCoveLocalDeviceProperties.LOCAL_DEVICE_PROPERTY_OPEN_CONNECTIONS));
		while (true) {
			if (transport.hasInput()) {
				console.send(transport.getInput());
			}
			if (console.hasInput()) {
				transport.send(console.getInput());
			}
			if (console.isClosed() || transport.isClosed()) {
				System.out.println("Closing");
				break;
			}
		}
		transport.close();
		console.close();
		
		// RemoteDevice transportConnection = foundDevices.get(name);
		// manager.connect(transport);
		/*
		while (true) {
			
			try {
				if (userInput.available() != 0) {
					readByte = userInput.read();
					if (readByte == -1) {
						System.out.println("Bad Input");
						break;
					}
					inBuffer[inPos] = (byte) readByte;
					if (inBuffer[inPos] == '\n') {
						if (inBuffer[inPos - 1] == '\r') {
							String temp = new String(inBuffer, 0, inPos + 1);
							os.write(temp.getBytes());
							inPos = 0;
							// mode = 1;
						}
					} else {
						inPos++;
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
*/
		//userInput.close();
	}

}
