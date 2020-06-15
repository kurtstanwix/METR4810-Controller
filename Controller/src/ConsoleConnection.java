import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;


// StreamConnection wrapper for custom in and out streams
public class ConsoleConnection implements StreamConnection {
	private InputStream input;
	private OutputStream output;
	
	public ConsoleConnection() {
		this(System.in, System.out);
	}
	
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
