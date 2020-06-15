import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.microedition.io.StreamConnection;

public class IOConnection implements Runnable {
	StreamConnection connection;
	private InputStream in;
	private OutputStream out;

	private Queue<String> inputToProcess = new ConcurrentLinkedQueue<>();
	private Queue<String> outputToProcess = new ConcurrentLinkedQueue<>();

	private String name;
	
	private AtomicBoolean closed = new AtomicBoolean(true);

	public IOConnection(String name, StreamConnection connection) {
		if (connection == null) {
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

	public void send(String toSend) {
		outputToProcess.add(toSend);
	}

	public boolean hasInput() {
		return !inputToProcess.isEmpty();
	}

	public String getInput() {
		return inputToProcess.poll();
	}
	
	public boolean isClosed() {
		return closed.get();
	}

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
		while (true) {
			synchronized (this) {
				if (closed.get()) {
					break;
				}
				try {
					if (in.available() != 0) {
						readByte = in.read();
						if (readByte == -1) {
							inputToProcess.add(name + " - Connection closed\r\n");
							break;
						}
						inBuffer[inPos] = (byte) readByte;
						if (inBuffer[inPos] == '\n') {
							if (inBuffer[inPos - 1] == '\r') {
								if (inPos == 1) {
									inputToProcess.add(name + " - Close Requested\r\n");
									break;
								}
								inputToProcess.add(new String(inBuffer, 0, inPos + 1));
								inPos = 0;
								// mode = 1;
							}
						} else {
							inPos++;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					this.close();
				}
				if (!outputToProcess.isEmpty()) {
					try {
						out.write(outputToProcess.poll().getBytes());
					} catch (IOException e) {
						e.printStackTrace();
						this.close();
					}
				}
			}
		}
		System.out.println("Closing " + name);
		this.close();
	}
}
