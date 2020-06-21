import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;

/**
 * Wrapper class for a StreamConnection to allow arbitrary underlying in and out
 * streams.
 */
public class ConsoleConnection implements StreamConnection {
	// Underlying input stream
	private InputStream input;
	// Underlying output stream
	private OutputStream output;

	/**
	 * Create a new Connection with sysin and sysout.
	 */
	public ConsoleConnection() {
		this(System.in, System.out);
	}

	/**
	 * Create a new Connection with arbitrary input and output
	 * 
	 * @param input
	 *            inputstream to connect to
	 * @param output
	 *            outputstream to connect to
	 */
	public ConsoleConnection(InputStream input, OutputStream output) {
		this.input = input;
		this.output = output;
	}

	@Override
	public DataInputStream openDataInputStream() throws IOException {
		return null;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return this.input;
	}

	@Override
	public void close() throws IOException {
		return; // Closing System.in or System.out would be bad
	}

	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		return null;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return this.output;
	}

}
