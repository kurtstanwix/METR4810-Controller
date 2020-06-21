import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class CommsFSM implements Runnable {
	// IO for the device this FSM is managing
	protected IOConnection device;
	// The Bluetooth manager to facilitate connection to device
	protected ControllerBT manager;
	// Bluetooth friendly name for the device
	protected String deviceName;

	// Waiting for Sequence Number U
	private static final int STATE_RCV_SEQ_U = 1;
	// Waiting for Sequence Number V
	private static final int STATE_RCV_SEQ_V = 2;
	// Controller's U Receiving (Device's X Sending) Sequence Number as per FSM
	private static final char RCV_SEQNUM_U = '2';
	// Controller's V Receiving (Device's Y Sending) Sequence Number as per FSM
	private static final char RCV_SEQNUM_V = '3';

	// Sending Sequence Number X state
	private static final int STATE_SND_SEQ_X = 1;
	// Waiting for Acknowledgement of X Sequence Number state
	private static final int STATE_SND_SEQ_X_ACK = 2;
	// Sending Sequence Number Y state
	private static final int STATE_SND_SEQ_Y = 3;
	// Waiting for Acknowledgement of Y Sequence Number state
	private static final int STATE_SND_SEQ_Y_ACK = 4;
	// Controller's X Sending (Device's U Receiving) Sequence Number as per FSM
	private static final char SND_SEQNUM_X = '0';
	// Controller's X Sending (Device's V Receiving) Sequence Number as per FSM
	private static final char SND_SEQNUM_Y = '1';

	// Current Receiving state of FSM
	private int rcvState = STATE_RCV_SEQ_U;
	// Current Sending state of FSM
	private int sndState = STATE_SND_SEQ_X;

	// Time to wait for acknowledgement before resending
	private static final long COMMS_TIMEOUT = 2000;

	// Flag for receiving and acknowledgement of Sequence Number X
	private boolean rcvACKX = false;
	// Flag for receiving and acknowledgement of Sequence Number Y
	private boolean rcvACKY = false;

	// Time of last sent packet
	private long sentTime;

	// FIFO Queue of packets to send
	private Queue<byte[]> outputQueue = new ConcurrentLinkedQueue<>();

	// FSM Thread closed flag
	private boolean closed = false;

	/**
	 * Connects to Bluetooth device with friendly name of deviceName and begins the
	 * communications Finite State Machine implementing the reliable data transfer
	 * protocol.
	 * 
	 * @param manager
	 *            Bluetooth manager to facilitate connection to device. A scan must
	 *            have been completed prior to initialisation.
	 * @param deviceName
	 *            friendly Bluetooth name of device to connect to.
	 */
	public CommsFSM(ControllerBT manager, String deviceName) {
		this.deviceName = deviceName;
		this.manager = manager;
		try {
			device = new IOConnection(deviceName, manager.connect(deviceName));
			// Only closed if failed to initialise, try
			if (!device.isClosed()) {
				new Thread(device).start();
			}
			Controller.send("Connected to: " + deviceName + "\r\n", false);
		} catch (InterruptedException e) {
			// Shouldn't get here, nothing interrupts connect
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName(deviceName + "FSM");
		// Process the FSM until closed
		while (!closed) {
			if (device.isClosed()) {
				Controller.send("Lost connection to " + deviceName + "\r\n", false);
				try {
					device = new IOConnection(deviceName, manager.connect(deviceName));
					if (!device.isClosed()) {
						new Thread(device).start();
						Controller.send("Reconnected to " + deviceName + "\r\n", false);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			processFSM();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// Do nothing
			}
		}
	}

	/**
	 * Takes a command as a String and sends it to the device if it's a valid
	 * command for that device. Implementations must check for their specific
	 * commands.
	 * 
	 * @param command
	 *            Command to send to device.
	 * @return true if the command was valid and sent, false otherwise.
	 */
	protected abstract boolean processAndSendCommand(String command);

	/**
	 * Processes a received command.
	 * 
	 * @param command
	 *            Received command to process.
	 */
	protected abstract void doReceivedCommand(String command);

	/**
	 * Send a packet to the device, correcting for any formatting issues.
	 * 
	 * @param bytes
	 *            packet bytes to send to device.
	 */
	public void send(byte[] bytes) {
		// Check if the right terminating characters for the modules are present.
		// Add them if not
		if (bytes[bytes.length - 2] != '\r') {
			if (bytes[bytes.length - 1] == '\n') {
				// Has ending \n but not \r
				bytes = Arrays.copyOf(bytes, bytes.length + 1);
				bytes[bytes.length - 2] = '\r';
				bytes[bytes.length - 1] = '\n';
			} else if (bytes[bytes.length - 1] != '\n') {
				// Has neither \r or \n
				bytes = Arrays.copyOf(bytes, bytes.length + 1);
				bytes[bytes.length - 1] = '\r';
			}
		}
		if (bytes[bytes.length - 1] != '\n') {
			// Has end \r but not \n
			bytes = Arrays.copyOf(bytes, bytes.length + 1);
			bytes[bytes.length - 1] = '\n';
		}
		outputQueue.add(bytes); // Will be processed in FSM
	}

	/**
	 * Returns if the device's closed flag has been set.
	 * 
	 * @return true if the device is closed.
	 */
	public boolean isClosed() {
		return device.isClosed();
	}

	/**
	 * Closes the device and the FSM.
	 */
	public void close() {
		device.close();
		closed = true;
	}

	/**
	 * Implements the FSM. Any packets received are checked for the correct sequence
	 * number and acknowledged. Any packets sent will wait for the acknowledgement.
	 * To be called periodically.
	 */
	public void processFSM() {
		hasInput: {
			if (device.hasInput()) {
				String received = device.getInput();
				if (received == null) {
					// Due to multithreading, can sometimes have get input be
					// true but retrieve null input. If so, break from processing
					break hasInput;
				}
				// Min length for ACK packet
				if (received.length() >= 5 && received.substring(1, 4).equals("ACK")) {
					if (received.charAt(4) == SND_SEQNUM_X) {
						rcvACKX = true;
					} else if (received.charAt(4) == SND_SEQNUM_Y) {
						rcvACKY = true;
					}
					Controller.send("Received ACK" + received.charAt(4) + " from " + deviceName + "\r\n", true);
				} else { // Not an ACK received
					char receivedSeqNum = received.charAt(0);
					Controller.send("Received from " + deviceName + " " + received, true);
					switch (rcvState) {
					case STATE_RCV_SEQ_U:
						if (receivedSeqNum == RCV_SEQNUM_U) {
							// Correct sequence number received, process packet and send ACK
							device.send("0ACK" + RCV_SEQNUM_U + "\r\n");
							doReceivedCommand(received.substring(1));
							rcvState = STATE_RCV_SEQ_V;
						}
						// Otherwise incorrect sequence number, discard packet
						break;
					case STATE_RCV_SEQ_V:
						if (receivedSeqNum == RCV_SEQNUM_V) {
							// Correct sequence number received, process packet and send ACK
							device.send("0ACK" + RCV_SEQNUM_V + "\r\n");
							doReceivedCommand(received.substring(1));
							rcvState = STATE_RCV_SEQ_U;
						}
						// Otherwise incorrect sequence number, discard packet
						break;
					}
				}
			}
		}
		switch (sndState) {
		case STATE_SND_SEQ_X:
			if (!outputQueue.isEmpty()) {
				byte[] toSend = outputQueue.peek(); // Don't remove yet as may not be ACK'd
				toSend[0] = SND_SEQNUM_X; // Prepend sequence number
				device.send(new String(toSend));
				sndState = STATE_SND_SEQ_X_ACK;
				sentTime = System.currentTimeMillis();
			}
			break;
		case STATE_SND_SEQ_X_ACK:
			if (rcvACKX) {
				outputQueue.poll(); // Remove from queue
				sndState = STATE_SND_SEQ_Y;
			} else if (System.currentTimeMillis() - sentTime > COMMS_TIMEOUT) {
				// Timed out, resend
				sndState = STATE_SND_SEQ_X;
			}
			break;
		case STATE_SND_SEQ_Y:
			if (!outputQueue.isEmpty()) {
				byte[] toSend = outputQueue.peek(); // Don't remove yet as may not be ACK'd
				toSend[0] = SND_SEQNUM_Y; // Prepend sequence number
				device.send(new String(toSend));
				sndState = STATE_SND_SEQ_Y_ACK;
				sentTime = System.currentTimeMillis();
			}
			break;
		case STATE_SND_SEQ_Y_ACK:
			if (rcvACKY) {
				outputQueue.poll(); // Remove from queue
				sndState = STATE_SND_SEQ_X;
			} else if (System.currentTimeMillis() - sentTime > COMMS_TIMEOUT) {
				// Timed out, resend
				sndState = STATE_SND_SEQ_Y;
			}
			break;
		}

		rcvACKX = false;
		rcvACKY = false;
	}
}
