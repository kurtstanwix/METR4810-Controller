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
	// Controller's X Sending (Device's U Receiving) Sequence Number as per FSM
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

	private long sentTime;

	private Queue<byte[]> outputQueue = new ConcurrentLinkedQueue<>();
	
	private boolean closed = false;

	public CommsFSM(ControllerBT manager, String deviceName) {
		this.deviceName = deviceName;
		this.manager = manager;
		try {
			device = new IOConnection(deviceName, manager.connect(deviceName));
			if (!device.isClosed()) {
				new Thread(device).start();
			}
			Controller.send("Connected to: " + deviceName + "\r\n");
		} catch (InterruptedException e) {
			// Shouldn't get here, nothing interrupts connect
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		Thread.currentThread().setName(deviceName + "FSM");
		while (!closed) {
			this.process();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected abstract boolean processAndSendCommand(String command);

	protected abstract void doReceivedCommand(String command);

	public void send(byte[] bytes) {
		byte[] toSend;
		// Check if the right terminating characters for the modules are present
		if (bytes[bytes.length - 2] != '\r') {
			if (bytes[bytes.length - 1] == '\n') {
				bytes = Arrays.copyOf(bytes, bytes.length + 1);
				bytes[bytes.length - 2] = '\r';
				bytes[bytes.length - 1] = '\n';
			} else if (bytes[bytes.length - 1] != '\n') {
				bytes = Arrays.copyOf(bytes, bytes.length + 1);
				bytes[bytes.length - 1] = '\r';
			}
		}
		if (bytes[bytes.length - 1] != '\n') {
			bytes = Arrays.copyOf(bytes, bytes.length + 1);
			bytes[bytes.length - 1] = '\n';
		}
		outputQueue.add(bytes);
	}

	public boolean isClosed() {
		return device.isClosed();
	}

	public void close() {
		device.close();
		closed = true;
	}

	public void process() {
		hasInput: {
			if (device.hasInput()) {
				String received = device.getInput();
				if (received == null) {
					// Due to multithreading, can sometimes have get input be true but retrieve null input
					break hasInput;
				}
				if (received.length() >= 5 && received.substring(1, 4).equals("ACK")) {
					if (received.charAt(4) == SND_SEQNUM_X) {
						rcvACKX = true;
					} else if (received.charAt(4) == SND_SEQNUM_Y) {
						rcvACKY = true;
					}
					Controller.send("Received ACK" + received.charAt(4) + " from " + deviceName + "\r\n");

				} else {
					char receivedSeqNum = received.charAt(0);
					switch (rcvState) {
					case STATE_RCV_SEQ_U:
						Controller.send("Received from " + deviceName + received);
						if (receivedSeqNum == RCV_SEQNUM_U) {
							// Correct sequence number received, extract packet,
							// set ready flag and send ACK
							device.send("0ACK" + RCV_SEQNUM_U + "\r\n");
							doReceivedCommand(received.substring(1));
							rcvState = STATE_RCV_SEQ_V;
						}
						// Otherwise incorrect sequence number, discard packet
						break;
					case STATE_RCV_SEQ_V:
						Controller.send(deviceName + " Received: " + received);
						if (receivedSeqNum == RCV_SEQNUM_V) {
							// Correct sequence number received, extract packet,
							// set ready flag and send ACK
							device.send("0ACK" + RCV_SEQNUM_V + "\r\n");
							doReceivedCommand(received.substring(1));
							rcvState = STATE_RCV_SEQ_U;
						}
						// Otherwise incorrect sequence number, discard packet
						break;
					}
				} /*
					 * else { debug.send("Invalid Command: " + received.substring(1)); }
					 */
			}
		}
		switch (sndState) {
		case STATE_SND_SEQ_X:
			if (!outputQueue.isEmpty()) {
				byte[] toSend = outputQueue.peek();
				toSend[0] = SND_SEQNUM_X;
				device.send(new String(toSend));
				sndState = STATE_SND_SEQ_X_ACK;
				sentTime = System.currentTimeMillis();
			}
			break;
		case STATE_SND_SEQ_X_ACK:
			if (rcvACKX) {
				outputQueue.poll();
				sndState = STATE_SND_SEQ_Y;
			} else if (System.currentTimeMillis() - sentTime > COMMS_TIMEOUT) {
				sndState = STATE_SND_SEQ_X;
			}
			break;
		case STATE_SND_SEQ_Y:
			if (!outputQueue.isEmpty()) {
				byte[] toSend = outputQueue.peek();
				toSend[0] = SND_SEQNUM_Y;
				device.send(new String(toSend));
				sndState = STATE_SND_SEQ_Y_ACK;
				sentTime = System.currentTimeMillis();
			}
			break;
		case STATE_SND_SEQ_Y_ACK:
			if (rcvACKY) {
				outputQueue.poll();
				sndState = STATE_SND_SEQ_X;
			} else if (System.currentTimeMillis() - sentTime > COMMS_TIMEOUT) {
				sndState = STATE_SND_SEQ_Y;
			}
			break;
		}

		rcvACKX = false;
		rcvACKY = false;
	}
}
