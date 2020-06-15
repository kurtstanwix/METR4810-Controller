import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class CommsFSM {

	protected IOConnection device;
	protected ControllerBT manager;
	protected String deviceName;

	private static final int STATE_RCV_SEQ_U = 1;
	private static final int STATE_RCV_SEQ_V = 2;
	private static final char RCV_SEQNUM_U = '2';
	private static final char RCV_SEQNUM_V = '3';

	private static final int STATE_SND_SEQ_X = 1;
	private static final int STATE_SND_SEQ_X_ACK = 2;
	private static final int STATE_SND_SEQ_Y = 3;
	private static final int STATE_SND_SEQ_Y_ACK = 4;
	private static final char SND_SEQNUM_X = '0';
	private static final char SND_SEQNUM_Y = '1';
	
	private int rcvState =  STATE_RCV_SEQ_U;
	private int sndState =  STATE_SND_SEQ_X;
	
	private static final long COMMS_TIMEOUT = 2000;
	
	private boolean rcvACKX = false;
	private boolean rcvACKY = false;
	
	private long sentTime;
	
	private Queue<byte[]> outputQueue = new ConcurrentLinkedQueue<>();
	
	public CommsFSM(ControllerBT manager, String deviceName) {
		this.deviceName = deviceName;
		this.manager = manager;
		try {
			device = new IOConnection(deviceName, manager.connect(deviceName));
			if (!device.isClosed()) {
				new Thread(device).start();
			}
			Controller.debug.send("Connected to: " + deviceName + "\r\n");
		} catch (InterruptedException e) {
			// Shouldn't get here, nothing interrupts connect
			e.printStackTrace();
		}
	}
	
	protected abstract void doCommand(String command);
	
	public void send(byte[] bytes) {
		outputQueue.add(bytes);
	}
	
	public boolean isClosed() {
		return device.isClosed();
	}
	
	public void close() {
		device.close();
	}
	
	public void process() {
		if (device.hasInput()) {
			String received = device.getInput();
			if (received.length() >= 5 && received.substring(1, 4).equals("ACK")) {
				if (received.charAt(4) == SND_SEQNUM_X) {
					rcvACKX = true;
				} else if (received.charAt(4) == SND_SEQNUM_Y) {
					rcvACKY = true;
				}
				Controller.debug.send(deviceName + "Received ACK:\r\n" + //
				"AckNum = " + //
				received.charAt(4) + //
				"\r\n");
				
			} else {
				char receivedSeqNum = received.charAt(0);
				switch (rcvState) {
					case STATE_RCV_SEQ_U:
						Controller.debug.send(deviceName + " Received: " + received);
						if (receivedSeqNum == RCV_SEQNUM_U) {
	                        // Correct sequence number received, extract packet,
	                        // set ready flag and send ACK
							device.send("0ACK" + RCV_SEQNUM_U + "\r\n");
							rcvState = STATE_RCV_SEQ_V;
						}
	                    // Otherwise incorrect sequence number, discard packet
						break;
					case STATE_RCV_SEQ_V:
						Controller.debug.send(deviceName + "Received: " + received);
						if (receivedSeqNum == RCV_SEQNUM_V) {
	                        // Correct sequence number received, extract packet,
	                        // set ready flag and send ACK
							device.send("0ACK" + RCV_SEQNUM_V + "\r\n");
							rcvState = STATE_RCV_SEQ_U;
						}
	                    // Otherwise incorrect sequence number, discard packet
						break;
				}
			}/* else {
				debug.send("Invalid Command: " + received.substring(1));
			}*/
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
