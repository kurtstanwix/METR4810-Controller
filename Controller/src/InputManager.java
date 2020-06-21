import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

public class InputManager implements KeyEventDispatcher {
	// Delay before sending another servo move (ms)
	private static final int SERVO_DELAY = 5;
	// Pulse width to increment joint 2 by
	private static final int JOINT2_STEP = 15;
	// Pulse width to increment joint 2 by
	private static final int JOINT4_STEP = 35;

	// Duty cycle for motor moving fast
	private static final int MOTOR_FAST_SPEED = 600;
	// Duty cycle for motor moving slow
	private static final int MOTOR_SLOW_SPEED = 300;

	// Booleans for key presses so multiple events don't occur for one long press
	private boolean pressedW = false;
	private boolean pressedA = false;
	private boolean pressedS = false;
	private boolean pressedD = false;
	private boolean pressedSpace = false;

	// Indicates if the end effector is grabbing
	private boolean grabbing = false;

	// Times the keys were last pressed to avoid flooding the events
	private long lastTimeE = System.currentTimeMillis() - SERVO_DELAY;
	private long lastTimeQ = System.currentTimeMillis() - SERVO_DELAY;
	private long lastTimeX = System.currentTimeMillis() - SERVO_DELAY;
	private long lastTimeZ = System.currentTimeMillis() - SERVO_DELAY;

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() instanceof JTextField) {
			return false;
		}
		Handler handler = Controller.getHandler();
		Transport transport = Controller.getTransport();
		if (Controller.mode == Controller.HANDLER_MODE && handler != null) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_W) { // Extend arm
					if (!pressedW) {
						Controller.send("w Pressed\r\n", true);
						handler.setDutyCycle(3, 500);
						pressedW = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_A) { // Rotate anticlockwise
					if (!pressedA) {
						Controller.send("a Pressed\r\n", true);
						handler.setDutyCycle(1, 500);
						pressedA = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_S) { // Retract arm
					if (!pressedS) {
						Controller.send("s Pressed\r\n", true);
						handler.setDutyCycle(3, -500);
						pressedS = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_D) { // Rotate clockwise
					if (!pressedD) {
						Controller.send("d Pressed\r\n", true);
						handler.setDutyCycle(1, -500);
						pressedD = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) { // Toggle end effector
					if (!pressedSpace) {
						Controller.send("space Pressed\r\n", true);
						if (grabbing) {
							handler.setPulseWidth(5, Controller.RELEASING_PULSE_WIDTH);
							grabbing = false;
						} else {
							handler.setPulseWidth(5, Controller.GRABBING_PULSE_WIDTH);
							grabbing = true;
						}
						pressedSpace = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_E) { // Rotate end effector clockwise
					if (System.currentTimeMillis() - lastTimeE > SERVO_DELAY) {
						Controller.send("e Pressed\r\n", true);
						handler.setPulseWidth(4, handler.getJoint4PulseWidth() + JOINT4_STEP);
						lastTimeE = System.currentTimeMillis();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_Q) { // Rotate end effector anticlockwise
					if (System.currentTimeMillis() - lastTimeQ > SERVO_DELAY) {
						Controller.send("q Pressed\r\n", true);
						handler.setPulseWidth(4, handler.getJoint4PulseWidth() - JOINT4_STEP);
						lastTimeQ = System.currentTimeMillis();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_X) { // Lower arm
					if (System.currentTimeMillis() - lastTimeX > SERVO_DELAY) {
						Controller.send("x Pressed\r\n", true);
						handler.setPulseWidth(2, handler.getJoint2PulseWidth() + JOINT2_STEP);
						lastTimeX = System.currentTimeMillis();
					}
				} else if (e.getKeyCode() == KeyEvent.VK_Z) { // Raise arm
					if (System.currentTimeMillis() - lastTimeZ > SERVO_DELAY) {
						Controller.send("z Pressed\r\n", true);
						handler.setPulseWidth(2, handler.getJoint2PulseWidth() - JOINT2_STEP);
						lastTimeZ = System.currentTimeMillis();
					}
				}
				// Release any flags and reset timers once key is released
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					Controller.send("w Released\r\n", true);
					handler.setDutyCycle(3, 0);
					pressedW = false;
				} else if (e.getKeyCode() == KeyEvent.VK_A) {
					Controller.send("a Released\r\n", true);
					handler.setDutyCycle(1, 0);
					pressedA = false;
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					Controller.send("s Released\r\n", true);
					handler.setDutyCycle(3, 0);
					pressedS = false;
				} else if (e.getKeyCode() == KeyEvent.VK_D) {
					Controller.send("d Released\r\n", true);
					handler.setDutyCycle(1, 0);
					pressedD = false;
				} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					Controller.send("space Released\r\n", true);
					pressedSpace = false;
				} else if (e.getKeyCode() == KeyEvent.VK_E) {
					// Ensure next button press will be acted on immediately
					lastTimeE = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("e Released\r\n", true);
				} else if (e.getKeyCode() == KeyEvent.VK_Q) {
					// Ensure next button press will be acted on immediately
					lastTimeQ = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("q Released\r\n", true);
				} else if (e.getKeyCode() == KeyEvent.VK_X) {
					// Ensure next button press will be acted on immediately
					lastTimeX = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("x Released\r\n", true);
				} else if (e.getKeyCode() == KeyEvent.VK_Z) {
					// Ensure next button press will be acted on immediately
					lastTimeZ = System.currentTimeMillis() - SERVO_DELAY;
					Controller.send("z Released\r\n", true);
				}
			}
		} else if (Controller.mode == Controller.TRANSPORT_MODE && transport != null) {
			if (e.getID() == KeyEvent.KEY_PRESSED) {
				if (e.getKeyCode() == KeyEvent.VK_W) { // Go forwards fast
					if (!pressedW) {
						Controller.send("w Transport Pressed\r\n", true);
						transport.setDutyCycle(-MOTOR_FAST_SPEED);
						pressedW = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_D) { // Go forwards slow
					if (!pressedD) {
						Controller.send("d Transport Pressed\r\n", true);
						transport.setDutyCycle(-MOTOR_SLOW_SPEED);
						pressedD = true;
					}
				} else if (e.getKeyCode() == KeyEvent.VK_S) { // Go backwards fast
					if (!pressedS) {
						Controller.send("s Transport Pressed\r\n", true);
						transport.setDutyCycle(MOTOR_FAST_SPEED);
						pressedS = true;
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_A) { // Go backwards slow
					if (!pressedA) {
						Controller.send("a Transport Pressed\r\n", true);
						transport.setDutyCycle(MOTOR_SLOW_SPEED);
						pressedA = true;
					}
				}
			} else if (e.getID() == KeyEvent.KEY_RELEASED) {
				if (e.getKeyCode() == KeyEvent.VK_W) {
					Controller.send("w Released\r\n", true);
					transport.setDutyCycle(0);
					pressedW = false;
				} else if (e.getKeyCode() == KeyEvent.VK_D) {
					Controller.send("d Released\r\n", true);
					transport.setDutyCycle(0);
					pressedD = false;
				} else if (e.getKeyCode() == KeyEvent.VK_S) {
					Controller.send("s transport Released\r\n", true);
					transport.setDutyCycle(0);
					pressedS = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_A) {
					Controller.send("a transport Released\r\n", true);
					transport.setDutyCycle(0);
					pressedA = false;
				}
			}
		}
		return false;
	}
}
