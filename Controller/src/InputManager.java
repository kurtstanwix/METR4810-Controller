import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class InputManager implements KeyEventDispatcher {
	private static final int SERVO_DELAY = 5;
	private static final int JOINT2_STEP = 15;
	private static final int JOINT4_STEP = 35;

	private static final int MOTOR_FAST_SPEED = 600;
	private static final int MOTOR_SLOW_SPEED = 300;

	boolean pressedW = false;
	boolean pressedA = false;
	boolean pressedS = false;
	boolean pressedD = false;

	boolean pressedSpace = false;
	boolean grabbing = false;

	long lastTimeE = System.currentTimeMillis() - SERVO_DELAY;
	long lastTimeQ = System.currentTimeMillis() - SERVO_DELAY;
	long lastTimeX = System.currentTimeMillis() - SERVO_DELAY;
	long lastTimeZ = System.currentTimeMillis() - SERVO_DELAY;

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextField) {
			return false;
		}
		Handler handler = Controller.getHandler();
		Transport transport = Controller.getTransport();
		if (Controller.mode == Controller.HANDLER_MODE && handler != null) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					if (!pressedW) {
						Controller.send("w Pressed\r\n");
						handler.setDutyCycle(3, -500);
						pressedW = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_A) {
					if (!pressedA) {
						Controller.send("a Pressed\r\n");
						handler.setDutyCycle(1, 500);
						pressedA = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					if (!pressedS) {
						Controller.send("s Pressed\r\n");
						handler.setDutyCycle(3, 500);
						pressedS = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_D) {
					if (!pressedD) {
						Controller.send("d Pressed\r\n");
						handler.setDutyCycle(1, -500);
						pressedD = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					if (!pressedSpace) {
						Controller.send("space Pressed\r\n");
						if (grabbing) {
							handler.setPulseWidth(5, Controller.RELEASING_PULSE_WIDTH);
							grabbing = false;
						} else {
							handler.setPulseWidth(5, Controller.GRABBING_PULSE_WIDTH);
							grabbing = true;
						}
						pressedSpace = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_E) {
					if (System.currentTimeMillis() - lastTimeE > SERVO_DELAY) {
						Controller.send("e Pressed\r\n");
						handler.setPulseWidth(4, handler.getJoint4PulseWidth() + JOINT4_STEP);
						lastTimeE = System.currentTimeMillis();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_Q) {
					if (System.currentTimeMillis() - lastTimeQ > SERVO_DELAY) {
						Controller.send("q Pressed\r\n");
						handler.setPulseWidth(4, handler.getJoint4PulseWidth() - JOINT4_STEP);
						lastTimeQ = System.currentTimeMillis();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_X) {
					if (System.currentTimeMillis() - lastTimeX > SERVO_DELAY) {
						Controller.send("x Pressed\r\n");
						handler.setPulseWidth(2, handler.getJoint2PulseWidth() + JOINT2_STEP);
						lastTimeX = System.currentTimeMillis();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_Z) {
					if (System.currentTimeMillis() - lastTimeZ > SERVO_DELAY) {
						Controller.send("z Pressed\r\n");
						handler.setPulseWidth(2, handler.getJoint2PulseWidth() - JOINT2_STEP);
						lastTimeZ = System.currentTimeMillis();
					}
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					Controller.send("w Released\r\n");
					handler.setDutyCycle(3, 0);
					pressedW = false;
				} else if (e.getKeyCode() == KeyEvent.VK_A) {
					Controller.send("a Released\r\n");
					handler.setDutyCycle(1, 0);
					pressedA = false;
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					Controller.send("s Released\r\n");
					handler.setDutyCycle(3, 0);
					pressedS = false;
				} else if (e.getKeyCode() == KeyEvent.VK_D) {
					Controller.send("d Released\r\n");
					handler.setDutyCycle(1, 0);
					pressedD = false;
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					Controller.send("space Released\r\n");
					pressedSpace = false;
				} else if (e.getKeyCode() == KeyEvent.VK_E) {
					// Ensure next button press will be acted on immediately
					lastTimeE = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("e Released\r\n");
				} else if (e.getKeyCode() == KeyEvent.VK_Q) {
					// Ensure next button press will be acted on immediately
					lastTimeQ = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("q Released\r\n");
				} else if (e.getKeyCode() == KeyEvent.VK_X) {
					// Ensure next button press will be acted on immediately
					lastTimeX = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("x Released\r\n");
				} else if (e.getKeyCode() == KeyEvent.VK_Z) {
					// Ensure next button press will be acted on immediately
					lastTimeZ = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("z Released\r\n");
				}
			}
		} else if (Controller.mode == Controller.TRANSPORT_MODE && transport != null) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					if (!pressedW) {
						Controller.send("w Transport Pressed\r\n");
						transport.setDutyCycle(-MOTOR_FAST_SPEED);
						pressedW = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					if (!pressedS) {
						Controller.send("s Transport Pressed\r\n");
						transport.setDutyCycle(-MOTOR_SLOW_SPEED);
						pressedS = true;
					}
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					Controller.send("w Released\r\n");
					transport.setDutyCycle(0);
					pressedW = false;
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					Controller.send("s transport Released\r\n");
					transport.setDutyCycle(0);
					pressedS = false;
				}
			}
		}
		return false;
	}
}
