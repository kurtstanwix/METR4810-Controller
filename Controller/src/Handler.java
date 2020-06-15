public class Handler extends CommsFSM {
	
	private static final String DEVICE_NAME = "HANDLER";
	private ControllerBT manager;

	public Handler(ControllerBT manager) {
		super(manager, DEVICE_NAME);
	}
	
	@Override
	protected void doCommand(String command) {
		if (command.length() >= 3) { // Minimum size for a command
			if (command.substring(0, 3).equals("MOV")) {
				Controller.debug.send("Got a MOV\r\n");
			}
		}
	}

}