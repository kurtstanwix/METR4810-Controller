import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.io.StreamConnection;

public class IOConnection implements Runnable {
	// Underlying Connection
	private StreamConnection connection;
	// Input of connection
	private InputStream in;
	// Output of connection
	private OutputStream out;

	// FIFO queue of String inputs
	private Queue<String> inputToProcess = new ConcurrentLinkedQueue<>();
	// FIFO queue of String outputs
	private Queue<String> outputToProcess = new ConcurrentLinkedQueue<>();

	// Name of the IOConnection for debugging
	private String name;

	// IO Thread closed flag
	private AtomicBoolean closed = new AtomicBoolean(true);

	/**
	 * Create an IO connection with the given name and input/output stream
	 * 
	 * @param name
	 *            name of the connection
	 * @param connection
	 *            connection containing the input and output streams
	 */
	public IOConnection(String name, StreamConnection connection) {
		if (connection == null) { // Don't connect to a null
			System.err.println("Null connection: " + name);
			return;
		}
		this.connection = connection;
		try {
			in = connection.openInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldn't open InputStream of " + name);
			return;
		}
		try {
			out = connection.openOutputStream();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Couldn't open OutputStream of " + name);
			try {
				in.close();
			} catch (IOException e2) {
				e2.printStackTrace();
				System.err.println("Failed to close InputStream of " + name);
			}
			return;
		}
		this.name = name;
		closed.set(false);
	}

	/**
	 * Send a string to the output stream
	 * 
	 * @param toSend
	 *            String to send to the output stream
	 */
	public void send(String toSend) {
		outputToProcess.add(toSend);
	}

	/**
	 * Checks if there is some input ready
	 * 
	 * @return true if input ready, false otherwise
	 */
	public boolean hasInput() {
		return !inputToProcess.isEmpty();
	}

	/**
	 * Get the next input from the input stream. Check hasInput first to ensure
	 * there is some input first.
	 * 
	 * @return next input from stream, or null if none
	 */
	public String getInput() {
		return inputToProcess.poll();
	}

	/**
	 * Returns if IO thread is closed
	 * 
	 * @return true if IO thread is closed, false otherwise
	 */
	public boolean isClosed() {
		return closed.get();
	}

	/**
	 * Closes the IO thread
	 */
	public synchronized void close() {
		if (closed.get()) {
			return;
		}
		try {
			connection.close();
		} catch (IOException e) {
			// Most likely already closed
			e.printStackTrace();
		}
		closed.set(true);
	}

	@Override
	public void run() {
		byte[] inBuffer = new byte[1024];
		int readByte = -1;
		int inPos = 0;
		Thread.currentThread().setName(name + "IO");
		while (true) {
			synchronized (this) {
				if (closed.get()) {
					break;
				}
				try {
					if (in != null) {
						if (in.available() != 0) { // There's a byte ready
							readByte = in.read();
							if (readByte == -1) { // Connection closed indicator
								inputToProcess.add(name + " - Connection closed\r\n");
								break;
							}
							inBuffer[inPos] = (byte) readByte;
							if (inBuffer[inPos] == '\n') {
								if (inBuffer[inPos - 1] == '\r') {
									// EOL received
									if (inPos == 1) { // Blank line, close connection
										inputToProcess.add(name + " - Close Requested\r\n");
										break;
									}
									// Add the received input to the queue
									inputToProcess.add(new String(inBuffer, 0, inPos + 1));
									inPos = 0;
								}
							} else {
								inPos++;
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					this.close();
				}
				if (!outputToProcess.isEmpty()) { // Got some output to send
					try {
						out.write(outputToProcess.poll().getBytes());
					} catch (IOException e) {
						e.printStackTrace();
						this.close();
					}
				}

			}
			try {
				Thread.sleep(5); // Or else this has a high CPU usage
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.err.println("Closing " + name);
		this.close();
	}
}
