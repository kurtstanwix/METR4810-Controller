import java.util.concurrent.atomic.AtomicBoolean;

public class Handler extends CommsFSM {
	// Handler's friendly Bluetooth device name to connect to
	private static final String DEVICE_NAME = "HANDLER";

	// Flag to indicate if still processing a packet
	private AtomicBoolean processing = new AtomicBoolean(false);
	// Flag to indicate an SPD packet was received
	private AtomicBoolean SPDReceived = new AtomicBoolean(false);
	// Flag to indicate an DEG packet was received
	private AtomicBoolean DEGReceived = new AtomicBoolean(false);

	// Minimum pulse width for a standard 90 degree servo
	private static final int JOINT_PULSE_WIDTH_MIN = 1000;
	// Maximum pulse width for a standard 90 degree servo
	private static final int JOINT_PULSE_WIDTH_MAX = 2000;

	// Minimum pulse width for end effector 180 degree servo
	private static final int END_EFFECTOR_PULSE_WIDTH_MIN = 700;
	// Maximum pulse width for end effector 180 degree servo
	private static final int END_EFFECTOR_PULSE_WIDTH_MAX = 2300;

	// Minimum duty cycle for the motors
	private static final int JOINT_DUTY_CYCLE_MIN = -1000;
	// Maximum duty cycle for the motors
	private static final int JOINT_DUTY_CYCLE_MAX = 1000;

	// Current duty cycle for joint 1
	private int joint1DutyCycle;
	// Current pulse width for joint 2
	private int joint2PulseWidth;
	// Current duty cycle for joint 3
	private int joint3DutyCycle;
	// Current pulse width for joint 4
	private int joint4PulseWidth;
	// Current pulse width for joint 5
	private int joint5PulseWidth;

	/**
	 * Sets up the state machine and IO with the specified Bluetooth manager
	 * 
	 * @param manager
	 *            Bluetooth manager to use to connect to Handler System
	 */
	public Handler(ControllerBT manager) {
		super(manager, DEVICE_NAME);
	}

	@Override
	protected void doReceivedCommand(String command) {
		if (command.length() >= 3) { // Minimum size for a command
			if (command.substring(0, 3).equals("DEG")) {
				// A DEG response was received, let the waiting thread know
				Controller.send("Got a DEG\r\n", false);
				DEGReceived.set(true);
			} else if (command.substring(0, 3).equals("SPD")) {
				// An SPD response was received, let the waiting thread know
				Controller.send("Got a SPD\r\n", false);
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
				if (command.substring(0, 3).equals("DEG")) {
					send(bytes);
					Controller.send("Sent DEG command\r\n", true);
					while (!DEGReceived.get())
						; // Wait for a response
					processing.set(false); // Done processing
					DEGReceived.set(false);
					return true;
					/*
					 * } else if (command.substring(0, 3).equals("POS")) { send(bytes);
					 * Controller.send("Sent POS command\r\n", true); return true;
					 */
				} else if (command.substring(0, 3).equals("SPD")) {
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
	 * Set the pulse width of a servo joint
	 * 
	 * @param jointNum
	 *            joint number of the servo
	 * @param pulseWidth
	 *            pulse width to set the servo to
	 * @return true if valid joint number, pulse width and command could send
	 */
	public boolean setPulseWidth(int jointNum, int pulseWidth) {
		// Check if a valid pulse width
		if ((jointNum == 4 && (pulseWidth < END_EFFECTOR_PULSE_WIDTH_MIN || pulseWidth > END_EFFECTOR_PULSE_WIDTH_MAX))
				|| (pulseWidth < JOINT_PULSE_WIDTH_MIN || pulseWidth > JOINT_PULSE_WIDTH_MAX)) {
			return false;
		}
		// Send with correct joint number and update internal pulse width value
		if (jointNum == 2) {
			if (processAndSendCommand(createPulseWidth(jointNum, pulseWidth))) {
				joint2PulseWidth = pulseWidth;
			} else {
				return false;
			}
		} else if (jointNum == 4) {
			if (processAndSendCommand(createPulseWidth(jointNum, pulseWidth))) {
				joint4PulseWidth = pulseWidth;
			} else {
				return false;
			}
		} else if (jointNum == 5) {
			if (processAndSendCommand(createPulseWidth(jointNum, pulseWidth))) {
				joint5PulseWidth = pulseWidth;
			} else {
				return false;
			}
		} else {
			return false; // Invalid joint number
		}
		Controller.send("Set Joint " + jointNum + " to " + pulseWidth, false);
		return true;
	}

	/**
	 * Set the duty cycle of a motor joint
	 * 
	 * @param jointNum
	 *            joint number of the motor
	 * @param dutyCycle
	 *            duty cycle to set the motor to
	 * @return true if valid joint number, duty cycle and command could send
	 */
	public boolean setDutyCycle(int jointNum, int dutyCycle) {
		if (dutyCycle < JOINT_DUTY_CYCLE_MIN || dutyCycle > JOINT_DUTY_CYCLE_MAX) {
			return false; // Invalid duty cycle
		}
		// Send with correct joint number and update internal duty cycle value
		if (jointNum == 1) {
			if (processAndSendCommand(createDutyCycle(jointNum, dutyCycle))) {
				joint1DutyCycle = dutyCycle;
			} else {
				return false;
			}
		} else if (jointNum == 3) {
			if (processAndSendCommand(createDutyCycle(jointNum, dutyCycle))) {
				joint3DutyCycle = dutyCycle;
			} else {
				return false;
			}
		} else {
			return false; // Invalid joint number
		}
		Controller.send("Set Joint " + jointNum + " to " + dutyCycle, false);
		return true;
	}

	/**
	 * Get the duty cycle of joint 1
	 * 
	 * @return duty cycle of joint 1
	 */
	public int getJoint1DutyCycle() {
		return joint1DutyCycle;
	}

	/**
	 * Get the pulse width of joint 2
	 * 
	 * @return pulse width of joint 2
	 */
	public int getJoint2PulseWidth() {
		return joint2PulseWidth;
	}

	/**
	 * Get the duty cycle of joint 3
	 * 
	 * @return duty cycle of joint 3
	 */
	public int getJoint3DutyCycle() {
		return joint3DutyCycle;
	}

	/**
	 * Get the pulse width of joint 4
	 * 
	 * @return pulse width of joint 4
	 */
	public int getJoint4PulseWidth() {
		return joint4PulseWidth;
	}

	/**
	 * Get the pulse width of joint 5
	 * 
	 * @return pulse width of joint 5
	 */
	public int getJoint5PulseWidth() {
		return joint5PulseWidth;
	}

	/**
	 * Create a pulse width command
	 * 
	 * @param jointNum
	 *            joint number to set the pulse width to
	 * @param pulseWidth
	 *            pulse width to set the joint at
	 * @return pulse width command
	 */
	public static String createPulseWidth(int jointNum, int pulseWidth) {
		return "DEG" + Integer.toString(jointNum) + "," + Integer.toString(pulseWidth) + "\r\n";
	}

	/**
	 * Create a duty cycle command
	 * 
	 * @param jointNum
	 *            joint number to set the duty cycle to
	 * @param pulseWidth
	 *            duty cycle to set the joint to
	 * @return duty cycle command
	 */
	public static String createDutyCycle(int jointNum, int dutyCycle) {
		return "SPD" + Integer.toString(jointNum) + "," + Integer.toString(dutyCycle) + "\r\n";
	}
}
