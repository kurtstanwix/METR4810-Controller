import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.StreamConnection;


// StreamConnection wrapper for system in and out
public class ConsoleConnection implements StreamConnection {

	@Override
	public DataInputStream openDataInputStream() throws IOException {
		return null;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return System.in;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public DataOutputStream openDataOutputStream() throws IOException {
		return null;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return System.out;
	}

}
