import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

public class IOConnection implements Runnable {
	StreamConnection connection;
	private InputStream in;
	private OutputStream out;

	private String input;
	private boolean hasInput = false;

	private String output;
	private boolean hasOutput = false;

	private String name;
	
	private boolean closed = true;

	public IOConnection(String name, StreamConnection connection) {
		if (connection == null) {
			System.out.println("Null connection: " + name);
			return;
		}
		this.connection = connection;
		try {
			in = connection.openInputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			out = connection.openOutputStream();
		} catch (IOException e1) {
			try {
				e1.printStackTrace();
				in.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
			return;
		}
		this.name = name;
		this.closed = false;
		System.out.println("Init: " + name);
	}

	public void send(String toSend) {
		this.output = toSend;
		this.hasOutput = true;
	}

	public boolean hasInput() {
		return this.hasInput;
	}

	public String getInput() {
		hasInput = false;
		return this.input;
	}
	
	public boolean isClosed() {
		return closed;
	}

	public synchronized void close() {
		if (closed) {
			return;
		}
		try {
			connection.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		closed = true;
	}

	@Override
	public void run() {
		byte[] inBuffer = new byte[1024];
		int readByte = -1;
		int inPos = 0;
		while (true) {
			synchronized (this) {
				if (closed) {
					break;
				}
				try {
					if (in.available() != 0) {
						readByte = in.read();
						if (readByte == -1) {
							input = name + " - Connection closed\r\n";
							hasInput = true;
							break;
						}
						inBuffer[inPos] = (byte) readByte;
						if (inBuffer[inPos] == '\n') {
							if (inBuffer[inPos - 1] == '\r') {
								input = new String(inBuffer, 0, inPos + 1);
								if (inPos == 1) {
									input = name + " - Close Requested\r\n";
									hasInput = true;
									break;
								}
								hasInput = true;
								inPos = 0;
								// mode = 1;
							}
						} else {
							inPos++;
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (hasOutput) {
					try {
						out.write(output.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
					hasOutput = false;
				}
			}
		}
		System.out.println("Closing " + name);
		this.close();
	}
}
