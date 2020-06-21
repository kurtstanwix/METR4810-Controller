import java.util.concurrent.atomic.AtomicBoolean;

public class Transport extends CommsFSM {
	// Transport's friendly Bluetooth device name to connect to
	private static final String DEVICE_NAME = "TRANSPORT";

	// Flag to indicate if still processing a packet
	private AtomicBoolean processing = new AtomicBoolean(false);
	// Flag to indicate an SPD packet was received
	private AtomicBoolean SPDReceived = new AtomicBoolean(false);

	// Minimum duty cycle for the motors
	private static final int DUTY_CYCLE_MIN = -1000;
	// Maximum duty cycle for the motors
	private static final int DUTY_CYCLE_MAX = 1000;

	// Current duty cycle of motor
	private int dutyCycle;

	/**
	 * Sets up the state machine and IO with the specified Bluetooth manager
	 * 
	 * @param manager
	 *            Bluetooth manager to use to connect to Transport System
	 */
	public Transport(ControllerBT manager) {
		super(manager, DEVICE_NAME);
	}

	@Override
	protected void doReceivedCommand(String command) {
		if (command.length() >= 3) { // Minimum size for a command
			if (command.substring(0, 3).equals("SPD")) {
				// An SPD response was received, let the waiting thread know
				Controller.send("Got a SPD\r\n", true);
				SPDReceived.set(true);
			}
		}
	}

	@Override
	protected boolean processAndSendCommand(String command) {
		if (!processing.get()) {
			// Start the processing
			processing.set(true);
			if (command.length() >= 3) { // Minimum size for a command
				// Convert to byte array and shift right by one to make room for sequence number
				byte[] bytes = new byte[command.length() + 1];
				byte[] temp = command.getBytes();
				for (int i = 0; i < temp.length; i++) {
					bytes[i + 1] = temp[i];
				}
				/*
				 * if (command.substring(0, 3).equals("MOV")) { send(bytes);
				 * Controller.send("Sent MOV command\r\n", true); return true; } else
				 */
				if (command.substring(0, 3).equals("SPD")) {
					send(bytes);
					Controller.send("Sent SPD command\r\n", true);
					while (!SPDReceived.get())
						; // Wait for a response
					processing.set(false); // Done processing
					SPDReceived.set(false);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the duty cycle of the motor
	 * 
	 * @return duty cycle of the motor
	 */
	public int getDutyCycle() {
		return dutyCycle;
	}

	/**
	 * Set the duty cycle of the motor
	 * 
	 * @param dutyCycle
	 *            duty cycle to set the motor to
	 * @return true if valid duty cycle and command could send
	 */
	public boolean setDutyCycle(int dutyCycle) {
		if (dutyCycle < DUTY_CYCLE_MIN || dutyCycle > DUTY_CYCLE_MAX) {
			return false;
		}
		if (processAndSendCommand(createDutyCycle(dutyCycle))) {
			this.dutyCycle = dutyCycle;
		} else {
			return false; // Couldn't send, don't update duty cycle
		}

		return true;
	}

	/**
	 * Create a duty cycle command
	 * 
	 * @param pulseWidth
	 *            duty cycle to set the motor to
	 * @return duty cycle command
	 */
	public static String createDutyCycle(int dutyCycle) {
		return "SPD" + Integer.toString(dutyCycle) + "\r\n";
	}
}
