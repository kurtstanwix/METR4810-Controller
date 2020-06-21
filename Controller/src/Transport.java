public class Transport extends CommsFSM {

	private static final String DEVICE_NAME = "TRANSPORT";
	private ControllerBT manager;

	private static final int DUTY_CYCLE_MIN = -1000;
	private static final int DUTY_CYCLE_MAX = 1000;

	private int dutyCycle;

	public Transport(ControllerBT manager) {
		super(manager, DEVICE_NAME);
	}
	
	@Override
	public void run() {
		while (true) {
			this.process();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doReceivedCommand(String command) {
		if (command.length() >= 3) { // Minimum size for a command
			if (command.substring(0, 3).equals("MOV")) {
				Controller.send("Got a MOV\r\n");
			}
		}
	}

	@Override
	protected boolean processAndSendCommand(String command) {
		if (command.length() >= 3) { // Minimum size for a command
			// Convert to byte array and shift right by one to make room for sequence number
			byte[] bytes = new byte[command.length() + 1];
			byte[] temp = command.getBytes();
			for (int i = 0; i < temp.length; i++) {
				bytes[i + 1] = temp[i];
			}
			if (command.substring(0, 3).equals("MOV")) {
				send(bytes);
				Controller.send("Sent MOV command\r\n");
				return true;
			} else if (command.substring(0, 3).equals("SPD")) {
				send(bytes);
				Controller.send("Sent SPD command\r\n");
				return true;
			}
		}
		return false;
	}
	
	public int getDutyCycle() {
		return dutyCycle;
	}

	public static String createDutyCycle(int dutyCycle) {
		return "SPD" + Integer.toString(dutyCycle) + "\r\n";
	}

	public boolean setDutyCycle(int dutyCycle) {
		if (dutyCycle < DUTY_CYCLE_MIN || dutyCycle > DUTY_CYCLE_MAX) {
			return false;
		}
		this.dutyCycle = dutyCycle;
		processAndSendCommand(createDutyCycle(dutyCycle));

		return true;
	}

}
