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

	private final static int WIDTH = 800;
	
	public GUI() {
		setupGUI();
	}
	
	public void print(String toPrint) {
		console.send(toPrint);
	}
	
	private void setupGUI() {
		JFrame frame = new JFrame();

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher(new InputManager());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		// frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(),
		// BoxLayout.PAGE_AXIS));
		
		JPanel modePanel = new JPanel();
		modePanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		JLabel modeLabel = new JLabel("Handler Mode");
		gbc.gridx = 0;
		gbc.gridy = 0;
		modePanel.add(modeLabel, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
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
		
		
		

		JPanel headerPanel = new JPanel();
		headerPanel.setFocusable(true);
		// headerPanel.addKeyListener(keys);
		headerPanel.setSize(WIDTH, 50);
		JPanel transportButtonPanel = new JPanel();
		transportButtonPanel.setFocusable(true);
		// transportButtonPanel.addKeyListener(keys);
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
					Controller.send("Set Transport Duty Cycle to: " + Integer.toString(transport.getDutyCycle()) + "\r\n");
				} else {
					Controller.send("Transport Duty Cycle at Maximum\r\n");
				}}
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
					Controller.send("Set Transport Duty Cycle to: " + Integer.toString(transport.getDutyCycle()) + "\r\n");
				} else {
					Controller.send("Transport Duty Cycle at Minimum\r\n");
				}}
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
				Controller.send("Set Transport Duty Cycle to Zero\r\n");
				}
			}
		});
		transportButtonPanel.add(transportZero, gbc);
		


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
					Controller.send("Set Joint 1 Duty Cycle to: " + Integer.toString(handler.getJoint1DutyCycle()) + "\r\n");
				} else {
					Controller.send("Joint 1 Duty Cycle at Maximum\r\n");
				}}
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
					Controller.send("Set Joint 1 Duty Cycle to: " + Integer.toString(handler.getJoint1DutyCycle()) + "\r\n");
				} else {
					Controller.send("Joint 1 Duty Cycle at Minimum\r\n");
				}}
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
				Controller.send("Set Joint 1 Duty Cycle to Zero\r\n");
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
					Controller.send("Set Joint 3 Duty Cycle to: " + Integer.toString(handler.getJoint3DutyCycle()) + "\r\n");
				} else {
					Controller.send("Joint 3 Duty Cycle at Maximum\r\n");
				}}
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
					Controller.send("Set Joint 3 Duty Cycle to: " + Integer.toString(handler.getJoint3DutyCycle()) + "\r\n");
				} else {
					Controller.send("Joint 3 Duty Cycle at Minimum\r\n");
				}}
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
				Controller.send("Set Joint 3 Duty Cycle to Zero\r\n");
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
					Controller.send("Set Joint 2 Pulse Width to: " + Integer.toString(handler.getJoint2PulseWidth()) + "\r\n");
				} else {
					Controller.send("Joint 2 Pulse Width at Maximum\r\n");
				}}
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
					Controller.send("Joint 2 Set Pulse Width to: " + Integer.toString(handler.getJoint2PulseWidth()) + "\r\n");
				} else {
					Controller.send("Joint 2 Pulse Width at Minimum\r\n");
				}}
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
					Controller.send("Set Joint 4 Pulse Width to: " + Integer.toString(handler.getJoint4PulseWidth()) + "\r\n");
				} else {
					Controller.send("Joint 4 Pulse Width at Maximum\r\n");
				}}
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
					Controller.send("Set Joint 4 Pulse Width to: " + Integer.toString(handler.getJoint4PulseWidth()) + "\r\n");
				} else {
					Controller.send("Joint 4 Pulse Width at Minimum\r\n");
				}}
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
					Controller.send("Set Joint 5 Pulse Width to: " + Integer.toString(handler.getJoint5PulseWidth()) + "\r\n");
				} else {
					Controller.send("Joint 5 Pulse Width at Maximum\r\n");
				}}
			}
		});
		handlerButtonPanel.add(joint5Increase, gbc);

		gbc.gridx = 4;
		gbc.gridy = 1;
		JButton join5Decrease = new JButton("5: Pulse Width - 20");
		join5Decrease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Handler handler = Controller.getHandler();
				if (handler != null) {
				if (handler.setPulseWidth(5, handler.getJoint5PulseWidth() - 20)) {
					Controller.send("Set Joint 5 Pulse Width to: " + Integer.toString(handler.getJoint5PulseWidth()) + "\r\n");
				} else {
					Controller.send("Joint 5 Pulse Width at Minimum\r\n");
				}}
			}
		});
		handlerButtonPanel.add(join5Decrease, gbc);

		// frame.addKeyListener(keys);

		JTextArea ta = new JTextArea();
		ta.setEditable(false);
		JScrollPane textScroll = new JScrollPane(ta);
		// textScroll.addKeyListener(keys);

		textScroll.setPreferredSize(new Dimension(WIDTH, 300));
		textScroll.setMinimumSize(new Dimension(WIDTH, 300));
		textScroll.setMaximumSize(new Dimension(WIDTH, 300));
		TextAreaOutputStream taos = new TextAreaOutputStream(ta, 60);
		// OutputStream ps = new PrintStream(taos);
		console = new IOConnection("Console", new ConsoleConnection(null, taos));
		if (!console.isClosed()) {
			new Thread(console).start();
		}

		JTextField textField = new JTextField(20);
		// textField.
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Controller.sendToBluetooth(textField.getText() + "\r\n");
				textField.setText(null);
			}

		});

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
