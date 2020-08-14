# ControllerJava

PC Controller for the METR4810 train yard cargo handler system team project.

Sends commands to manipulate the Transport and Handler systems to move the cargo containers to their respective coloured yards.

## Usage

The PC's friendly Bluetooth name must be "Controller" in order to prevent the system's from rejecting the connection, as a security measure.

Pressing the "Turn on Debug" button will show the commands sent to the controller along with any received commands and ACK's for diagnosing any communications issues.

There are two input modes: Transport and Handler.
The input received will send commands to their respective systems as per below:

### Transport Mode

w: Go forward at fast speed  
s: Go backward at fast speed  
d: Go forward at slow speed  
a: Go backward at slow speed  

### Handler Mode

w: Extend the arm  
s: Retract the arm  
d: Rotate the arm clockwise  
a: Rotate the arm anticlockwise  
space: Toggle the end effector open/close  
e: Rotate the end effector clockwise  
q: Rotate the end effector anticlockwise  
x: Lower the arm  
z: Raise the arm