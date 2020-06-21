import java.util.concurrent.atomic.AtomicBoolean;

public class Handler extends CommsFSM {

	private static final String DEVICE_NAME = "HANDLER";
	private ControllerBT manager;

	private AtomicBoolean processing = new AtomicBoolean(false);
	private AtomicBoolean SPDReceived = new AtomicBoolean(false);
	private AtomicBoolean DEGReceived = new AtomicBoolean(false);
	//private AtomicBoolean processing = new AtomicBoolean(false);
	
	private static final int JOINT_PULSE_WIDTH_MIN = 1000;
	private static final int JOINT_PULSE_WIDTH_MAX = 2000;
	
	private static final int JOINT_DUTY_CYCLE_MIN = -1000;
	private static final int JOINT_DUTY_CYCLE_MAX = 1000;

	private int joint1DutyCycle;
	private int joint2PulseWidth;
	private int joint3DutyCycle;
	private int joint4PulseWidth;
	private int joint5PulseWidth;

	public Handler(ControllerBT manager) {
		super(manager, DEVICE_NAME);
	}

	@Override
	protected void doReceivedCommand(String command) {
		if (command.length() >= 3) { // Minimum size for a command
			if (command.substring(0, 3).equals("DEG")) {
				Controller.send("Got a DEG\r\n");
				DEGReceived.set(true);
			} else if (command.substring(0,3).equals("SPD")) {
				Controller.send("Got a SPD\r\n");
				SPDReceived.set(true);
			}
		}
	}

	@Override
	protected boolean processAndSendCommand(String command) {
		if (!processing.get()) {
			processing.set(true);
			byte[] bytes = new byte[command.length() + 1];
			byte[] temp = command.getBytes();
			for (int i = 0; i < temp.length; i++) {
				bytes[i + 1] = temp[i];
			}
			if (command.length() >= 3) { // Minimum size for a command
				// Convert to byte array and shift right by one to make room for sequence number
				if (command.substring(0, 3).equals("DEG")) {
					send(bytes);
					Controller.send("Sent DEG command\r\n");
					while (!DEGReceived.get());
					processing.set(false);
					DEGReceived.set(false);
					return true;
				} else if (command.substring(0, 3).equals("POS")) {
					send(bytes);
					Controller.send("Sent POS command\r\n");
					return true;
				} else if (command.substring(0, 3).equals("SPD")) {
					send(bytes);
					Controller.send("Sent SPD command\r\n");
					while (!SPDReceived.get());
					processing.set(false);
					SPDReceived.set(false);
					return true;
				}
			}
			if (command.length() >= 4) {
				if (command.substring(0, 4).equals("TEST")) {
					send(bytes);
					Controller.send("Sent TEST command\r\n");
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean setPulseWidth(int jointNum, int pulseWidth) {
		if (pulseWidth < JOINT_PULSE_WIDTH_MIN || pulseWidth > JOINT_PULSE_WIDTH_MAX) {
			return false;
		}
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
		}
		return true;
	}
	
	public boolean setDutyCycle(int jointNum, int dutyCycle) {
		if (dutyCycle < JOINT_DUTY_CYCLE_MIN || dutyCycle > JOINT_DUTY_CYCLE_MAX) {
			return false;
		}
		if (jointNum == 1) {
			if (processAndSendCommand(createDutyCycle(jointNum, dutyCycle))) {
				joint1DutyCycle = dutyCycle;
			} else {
				return false;
			}
		}
		if (jointNum == 3) {
			if (processAndSendCommand(createDutyCycle(jointNum, dutyCycle))) {
				joint3DutyCycle = dutyCycle;
			} else {
				return false;
			}
			
		}
		return true;
	}

	
	public int getJoint1DutyCycle() {
		return joint1DutyCycle;
	}
	
	public int getJoint2PulseWidth() {
		return joint2PulseWidth;
	}

	public int getJoint3DutyCycle() {
		return joint3DutyCycle;
	}
	
	public int getJoint4PulseWidth() {
		return joint4PulseWidth;
	}
	
	public int getJoint5PulseWidth() {
		return joint5PulseWidth;
	}

	public static String createPulseWidth(int jointNum, int pulseWidth) {
		return "DEG" + Integer.toString(jointNum) + "," + Integer.toString(pulseWidth) + "\r\n";
	}

	public static String createDutyCycle(int jointNum, int dutyCycle) {
		return "SPD" + Integer.toString(jointNum) + "," + Integer.toString(dutyCycle) + "\r\n";
	}
}
