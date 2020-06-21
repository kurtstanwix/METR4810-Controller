import java.awt.EventQueue;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextArea;

public class TextAreaOutputStream extends OutputStream {
	// Array for write(int val);
	private byte[] oneByte;
	// Appender to stitch together the data
	private Appender appender;

	/**
	 * Create a stream to write to a text area.
	 * 
	 * @param txtara
	 *            text area to write to
	 */
	public TextAreaOutputStream(JTextArea txtArea) {
		this(txtArea, 1000);
	}

	/**
	 * Create a stream to write to a text area.
	 * 
	 * @param txtara
	 *            text area to write to
	 * @param maxlin
	 *            maximum number of lines to hold in text area
	 */
	public TextAreaOutputStream(JTextArea txtara, int maxlin) {
		if (maxlin < 1) {
			throw new IllegalArgumentException(
					"TextAreaOutputStream maximum lines must be positive (value=" + maxlin + ")");
		}
		oneByte = new byte[1];
		appender = new Appender(txtara, maxlin);
	}

	/**
	 * Clear the current console text area.
	 */
	public synchronized void clear() {
		if (appender != null) {
			appender.clear();
		}
	}

	@Override
	public synchronized void close() {
		appender = null;
	}

	@Override
	public synchronized void flush() {
	}

	@Override
	public synchronized void write(int val) {
		oneByte[0] = (byte) val;
		write(oneByte, 0, 1);
	}

	@Override
	public synchronized void write(byte[] ba) {
		write(ba, 0, ba.length);
	}

	@Override
	public synchronized void write(byte[] ba, int str, int len) {
		if (appender != null) {
			appender.append(bytesToString(ba, str, len));
		}
	}

	/**
	 * Convert the byte array to a string in the correct encoding
	 * 
	 * @param ba
	 *            bytes to convert
	 * @param str
	 *            offset into the byte array to start
	 * @param len
	 *            number of characters to convert
	 * @return String of converted bytes
	 */
	static private String bytesToString(byte[] ba, int str, int len) {
		try {
			return new String(ba, str, len, "UTF-8");
		} catch (UnsupportedEncodingException thr) {
			return new String(ba, str, len);
		} // All JVMs are required to support UTF-8
	}

	static class Appender implements Runnable {
		private final JTextArea textArea;
		// Maximum lines allowed in text area
		private final int maxLines;
		// Length of lines within text area
		private final LinkedList<Integer> lengths;
		// Values waiting to be appended
		private final List<String> values;

		// Length of current line
		private int curLength;
		// Flag to clear buffer
		private boolean clear;
		// Queue of items to append
		private boolean queue;

		Appender(JTextArea txtara, int maxlin) {
			textArea = txtara;
			maxLines = maxlin;
			lengths = new LinkedList<Integer>();
			values = new ArrayList<String>();

			curLength = 0;
			clear = false;
			queue = true;
		}

		/**
		 * Add a string to the list
		 * 
		 * @param val
		 *            string to add to list
		 */
		synchronized void append(String val) {
			values.add(val);
			if (queue) {
				queue = false;
				EventQueue.invokeLater(this);
			}
		}

		/**
		 * Clear all the output in the list
		 */
		synchronized void clear() {
			clear = true;
			curLength = 0;
			lengths.clear();
			values.clear();
			if (queue) {
				queue = false;
				EventQueue.invokeLater(this);
			}
		}

		// MUST BE THE ONLY METHOD THAT TOUCHES textArea!
		@Override
		public synchronized void run() {
			if (clear) {
				textArea.setText("");
			}
			for (String val : values) {
				curLength += val.length();
				if (val.endsWith(EOL1) || val.endsWith(EOL2)) {
					if (lengths.size() >= maxLines) {
						textArea.replaceRange("", 0, lengths.removeFirst());
					}
					lengths.addLast(curLength);
					curLength = 0;
				}
				textArea.append(val);
			}
			values.clear();
			clear = false;
			queue = true;
		}

		static private final String EOL1 = "\r\n";
		static private final String EOL2 = System.getProperty("line.separator", EOL1);
	}

}
