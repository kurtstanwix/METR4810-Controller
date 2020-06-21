import java.util.ArrayList;
import java.util.List;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;

public class Controller {
	// How many attempts to check for Bluetooth devices before exiting
	private static final int INQUIRY_ATTEMPTS = 3;
	
	// Instance of Transport System's FSM
	private static Transport transport;

	// Instance of Handler System's FSM
	private static Handler handler;

	// Wrapper of the terminal for IO
	private static IOConnection debug;
	
	// Instance for the GUI, only one should be active
	private static GUI gui;

	// Send commands to the Handler System
	public static final int HANDLER_MODE = 0;
	// Send commands to the Transport System
	public static final int TRANSPORT_MODE = 1;
	// Identifies which system commands are to be sent to
	public static int mode = HANDLER_MODE;
	
	// Pulse width for end effector to release container
	public static final int RELEASING_PULSE_WIDTH = 1600;

	// Pulse width for end effector to hold container
	public static final int GRABBING_PULSE_WIDTH = 1040;

	/**
	 * Get the instance of the Handler System's FSM
	 * @return Handler System FSM instance. Null if it is not connected
	 */
	public static Handler getHandler() {
		return handler;
	}

	/**
	 * Get the instance of the Transport System's FSM
	 * @return Transport System FSM instance. Null if it is not connected
	 */
	public static Transport getTransport() {
		return transport;
	}
	
	/**
	 * Sends a String the terminal and GUI console
	 * @param toSend String to display
	 */
	public static void send(String toSend) {
		if (gui != null) {
			gui.print(toSend);
		}
		if (debug != null) {
			debug.send(toSend);
		}
	}

	/**
	 * Send a String to the Transport or Handler System if prefixed by 'T' or
	 * 'H' respectively.
	 * @param toSend String to send, prefixed with 'T' or 'H' for destination.
	 */
	public static void sendToBluetooth(String toSend) {
		if (toSend.charAt(0) == 'T') {
			if (transport != null) {
				// Send to Transport, removing the prefixed 'T'
				transport.processAndSendCommand(toSend.substring(1));
			}
		} else if (toSend.charAt(0) == 'H') {
			if (handler != null) {
				// Send to Handler, removing the prefixed 'H'
				handler.processAndSendCommand(toSend.substring(1));
			}
		} else {
			send("Invalid Device\r\n");
		}
	}

	public static void main(String[] args) {
		GUI gui = new GUI();
		
		// A system in and out wrapper
		debug = new IOConnection("Debug", new ConsoleConnection());
		if (!debug.isClosed()) {
			new Thread(debug).start();
		}

		LocalDevice controllerBTDevice = null;
		try {
			controllerBTDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			debug.send("No Bluetooth host device detected.\r\n");
			System.exit(1); // Need to configure PC to have bluetooth
		}
		
		// Set how long the device spends enquiring for other devices
		BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_INQUIRY_DURATION, "3");

		ControllerBT managerBT = new ControllerBT(controllerBTDevice);

		// Find the BT devices in the area
		List<String> foundDevices = null;
		int scanAttempts = 0;
		foundDevices = new ArrayList<>();

		for (; scanAttempts < INQUIRY_ATTEMPTS; scanAttempts++) {
			try {
				foundDevices = managerBT.findDevices();
			} catch (InterruptedException e) { // Shouldn't get here, nothing interrupts findDevices
				e.printStackTrace();
			}
			if (foundDevices != null && !foundDevices.isEmpty()) {
				break; // Found some devices
			}
		}
		if (scanAttempts == INQUIRY_ATTEMPTS) {
			System.err.println("Scan attempts yielded no devices.");
			System.exit(1); // Turn on system modules
		}

		// List out the found devices, for debug purposes
		send("Found Devices:\r\n");
		for (String device : foundDevices) {
			send(String.format("  Name: \"%s\"\r\n", device));
		}

		// Set up the Handler System's comms FSM thread
		handler = new Handler(managerBT);
		new Thread(handler).start();

		// Set up the Transport System's comms FSM thread
		transport = new Transport(managerBT);
		new Thread(transport).start();
		
		// Set handler initial conditions
		if (handler != null) {
			while(!handler.setPulseWidth(2, 1500));
			while(!handler.setPulseWidth(4, 1500));
			while(!handler.setPulseWidth(5, RELEASING_PULSE_WIDTH));
		}

		while (true) {
			if (debug.hasInput()) {
				// Send any input from terminal to appropriate Bluetooth
				sendToBluetooth(debug.getInput());
			}
			if (debug.isClosed()) { // Should only happen if a blank line is sent in terminal
				System.err.println("Closing");
				break;
			}
		}
		// Close requested from terminal, shutdown threads
		if (handler != null) {
			handler.close();
		}
		if (transport != null) {
			transport.close();
		}
		if (debug != null) {
			debug.close();
		}
	}
}
