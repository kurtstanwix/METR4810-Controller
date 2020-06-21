import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI {
	// Output display for GUI's console log
	private static IOConnection console;

	// Width of GUI
	private final static int WIDTH = 800;

	/**
	 * Setup and display the GUI. Should only be initialised once.
	 */
	public GUI() {
		setupGUI();
	}

	/**
	 * Print a string to the console
	 * 
	 * @param toPrint
	 *            String to print to console
	 */
	public void print(String toPrint) {
		console.send(toPrint);
	}

	/**
	 * Setup and display the GUI
	 */
	private void setupGUI() {
		JFrame frame = new JFrame();

		// Setup the input handler for system control
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new InputManager());

		// Panel to hold all the content
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// Panel to hold the status indicators and buttons
		JPanel modePanel = new JPanel();
		modePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JLabel modeLabel = new JLabel("Handler Mode");
		gbc.gridx = 0;
		gbc.gridy = 0;
		modePanel.add(modeLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		// Button to switch control to Handler or Transport System
		JButton modeButton = new JButton("Switch to Transport Mode");
		modeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Controller.mode == Controller.HANDLER_MODE) {
					Controller.mode = Controller.TRANSPORT_MODE;
					modeButton.setText("Switch to Handler Mode");
					modeLabel.setText("Transport Mode");
				} else if (Controller.mode == Controller.TRANSPORT_MODE) {
					Controller.mode = Controller.HANDLER_MODE;
					modeButton.setText("Switch to Transport Mode");
					modeLabel.setText("Handler Mode");
				}
			}
		});
		modePanel.add(modeButton, gbc);

		JLabel debugLabel = new JLabel("Debug: Off");
		gbc.gridx = 1;
		gbc.gridy = 0;
		modePanel.add(debugLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		// Button to toggle debug output
		JButton debugButton = new JButton("Turn on Debug");
		debugButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Controller.getDebug()) {
					Controller.setDebug(false);
					debugButton.setText("Turn On Debug");
					debugLabel.setText("Debug: Off");
				} else {
					Controller.setDebug(true);
					debugButton.setText("Turn Off Debug");
					debugLabel.setText("Debug: On");
				}
			}
		});
		modePanel.add(debugButton, gbc);

		// Panel for control buttons
		JPanel headerPanel = new JPanel();
		headerPanel.setFocusable(true);
		headerPanel.setSize(WIDTH, 50);
		// Panel for the Transport System buttons
		JPanel transportButtonPanel = new JPanel();
		transportButtonPanel.setFocusable(true);
		headerPanel.add(transportButtonPanel, BorderLayout.WEST);
		transportButtonPanel.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		JButton transportIncrease = new JButton("T: Duty Cycle + 10");
		transportIncrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Transport transport = Controller.getTransport();
				if (transport != null) {
					if (transport.setDutyCycle(transport.getDutyCycle() + 10)) {
						Controller.send(
								"Set Transport Duty Cycle to: " + Integer.toString(transport.getDutyCycle()) + "\r\n",
								false);
					} else {
						Controller.send("Transport Duty Cycle at Maximum\r\n", false);
					}
				}
			}
		});
		transportButtonPanel.add(transportIncrease, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		JButton transportDecrease = new JButton("T: Duty Cycle - 10");
		transportDecrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Transport transport = Controller.getTransport();
				if (transport != null) {
					if (transport.setDutyCycle(transport.getDutyCycle() - 10)) {
						Controller.send(
								"Set Transport Duty Cycle to: " + Integer.toString(transport.getDutyCycle()) + "\r\n",
								false);
					} else {
						Controller.send("Transport Duty Cycle at Minimum\r\n", false);
					}
				}
			}
		});
		transportButtonPanel.add(transportDecrease, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		JButton transportZero = new JButton("T: Duty Cycle = 0");
		transportZero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					handler.setDutyCycle(1, 0);
					Controller.send("Set Transport Duty Cycle to Zero\r\n", false);
				}
			}
		});
		transportButtonPanel.add(transportZero, gbc);

		// Panel for the Handler System buttons
		JPanel handlerButtonPanel = new JPanel();
		handlerButtonPanel.setFocusable(true);
		headerPanel.add(handlerButtonPanel, BorderLayout.EAST);
		handlerButtonPanel.setLayout(new GridBagLayout());

		gbc.gridx = 0;
		gbc.gridy = 0;
		JButton joint1Increase = new JButton("1: Duty Cycle + 10");
		joint1Increase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setDutyCycle(1, handler.getJoint1DutyCycle() + 10)) {
						Controller.send(
								"Set Joint 1 Duty Cycle to: " + Integer.toString(handler.getJoint1DutyCycle()) + "\r\n",
								false);
					} else {
						Controller.send("Joint 1 Duty Cycle at Maximum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint1Increase, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		JButton joint1Decrease = new JButton("1: Duty Cycle - 10");
		joint1Decrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setDutyCycle(1, handler.getJoint1DutyCycle() - 10)) {
						Controller.send(
								"Set Joint 1 Duty Cycle to: " + Integer.toString(handler.getJoint1DutyCycle()) + "\r\n",
								false);
					} else {
						Controller.send("Joint 1 Duty Cycle at Minimum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint1Decrease, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		JButton joint1Zero = new JButton("1: Duty Cycle = 0");
		joint1Zero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					handler.setDutyCycle(1, 0);
					Controller.send("Set Joint 1 Duty Cycle to Zero\r\n", false);
				}
			}
		});
		handlerButtonPanel.add(joint1Zero, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		JButton joint3Increase = new JButton("3: Duty Cycle + 10");
		joint3Increase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setDutyCycle(3, handler.getJoint3DutyCycle() + 10)) {
						Controller.send(
								"Set Joint 3 Duty Cycle to: " + Integer.toString(handler.getJoint3DutyCycle()) + "\r\n",
								false);
					} else {
						Controller.send("Joint 3 Duty Cycle at Maximum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint3Increase, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		JButton joint3Decrease = new JButton("3: Duty Cycle - 10");
		joint3Decrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setDutyCycle(3, handler.getJoint3DutyCycle() - 10)) {
						Controller.send(
								"Set Joint 3 Duty Cycle to: " + Integer.toString(handler.getJoint3DutyCycle()) + "\r\n",
								false);
					} else {
						Controller.send("Joint 3 Duty Cycle at Minimum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint3Decrease, gbc);

		gbc.gridx = 1;
		gbc.gridy = 2;
		JButton joint3Zero = new JButton("3: Duty Cycle = 0");
		joint3Zero.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					handler.setDutyCycle(1, 0);
					Controller.send("Set Joint 3 Duty Cycle to Zero\r\n", false);
				}
			}
		});
		handlerButtonPanel.add(joint3Zero, gbc);

		gbc.gridx = 2;
		gbc.gridy = 0;
		JButton joint2Increase = new JButton("2: Pulse Width + 20");
		joint2Increase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setPulseWidth(2, handler.getJoint2PulseWidth() + 20)) {
						Controller.send("Set Joint 2 Pulse Width to: " + Integer.toString(handler.getJoint2PulseWidth())
								+ "\r\n", false);
					} else {
						Controller.send("Joint 2 Pulse Width at Maximum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint2Increase, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		JButton joint2Decrease = new JButton("2: Pulse Width - 20");
		joint2Decrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setPulseWidth(2, handler.getJoint2PulseWidth() - 20)) {
						Controller.send("Joint 2 Set Pulse Width to: " + Integer.toString(handler.getJoint2PulseWidth())
								+ "\r\n", false);
					} else {
						Controller.send("Joint 2 Pulse Width at Minimum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint2Decrease, gbc);

		gbc.gridx = 3;
		gbc.gridy = 0;
		JButton joint4Increase = new JButton("4: Pulse Width + 20");
		joint4Increase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setPulseWidth(4, handler.getJoint4PulseWidth() + 20)) {
						Controller.send("Set Joint 4 Pulse Width to: " + Integer.toString(handler.getJoint4PulseWidth())
								+ "\r\n", false);
					} else {
						Controller.send("Joint 4 Pulse Width at Maximum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint4Increase, gbc);

		gbc.gridx = 3;
		gbc.gridy = 1;
		JButton joint4Decrease = new JButton("4: Pulse Width - 20");
		joint4Decrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setPulseWidth(4, handler.getJoint4PulseWidth() - 20)) {
						Controller.send("Set Joint 4 Pulse Width to: " + Integer.toString(handler.getJoint4PulseWidth())
								+ "\r\n", false);
					} else {
						Controller.send("Joint 4 Pulse Width at Minimum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint4Decrease, gbc);

		gbc.gridx = 4;
		gbc.gridy = 0;
		JButton joint5Increase = new JButton("5: Pulse Width + 20");
		joint5Increase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setPulseWidth(5, handler.getJoint5PulseWidth() + 20)) {
						Controller.send("Set Joint 5 Pulse Width to: " + Integer.toString(handler.getJoint5PulseWidth())
								+ "\r\n", false);
					} else {
						Controller.send("Joint 5 Pulse Width at Maximum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint5Increase, gbc);

		gbc.gridx = 4;
		gbc.gridy = 1;
		JButton joint5Decrease = new JButton("5: Pulse Width - 20");
		joint5Decrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
					if (handler.setPulseWidth(5, handler.getJoint5PulseWidth() - 20)) {
						Controller.send("Set Joint 5 Pulse Width to: " + Integer.toString(handler.getJoint5PulseWidth())
								+ "\r\n", false);
					} else {
						Controller.send("Joint 5 Pulse Width at Minimum\r\n", false);
					}
				}
			}
		});
		handlerButtonPanel.add(joint5Decrease, gbc);

		// Text output for console
		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		// Makes it scrollable
		JScrollPane textScroll = new JScrollPane(ta);

		textScroll.setPreferredSize(new Dimension(WIDTH, 300));
		textScroll.setMinimumSize(new Dimension(WIDTH, 300));
		textScroll.setMaximumSize(new Dimension(WIDTH, 300));
		// Wraps output into a stream
		TextAreaOutputStream taos = new TextAreaOutputStream(ta, 300);
		// Wraps this stream into the custom IO API for easy integration
		console = new IOConnection("Console", new ConsoleConnection(null, taos));
		if (!console.isClosed()) {
			new Thread(console).start();
		}

		// Console input
		JTextField textField = new JTextField(20);
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Send any input to the respective Bluetooth device and clear the field
				Controller.sendToBluetooth(textField.getText() + "\r\n");
				textField.setText(null);
			}

		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Add all the components
		mainPanel.setFocusable(true);
		mainPanel.add(modePanel);
		mainPanel.add(headerPanel);
		mainPanel.add(textScroll);
		mainPanel.add(textField);
		frame.setContentPane(mainPanel);

		// Listener to remove focus from the input text field when clicked away
		mainPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseReleased(e);
				mainPanel.grabFocus();
			}
		});

		frame.pack();

		frame.setFocusable(true);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

}
