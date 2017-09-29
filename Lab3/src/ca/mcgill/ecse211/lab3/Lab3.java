// Lab2.java

package ca.mcgill.ecse211.lab3;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab3 {

	static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("B"));

	static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));

	static final EV3LargeRegulatedMotor eyesMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	
	static final Port usPort = LocalEV3.get().getPort("S1");

	
	public static final double WHEEL_RADIUS = 2.1;
	public static final double TRACK = 15.55;	//TODO: check the actual width of our robot

	public static void main(String[] args) {
		int buttonChoice;

		final TextLCD t = LocalEV3.get().getTextLCD();
		final Odometer odometer = new Odometer(leftMotor, rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
		OdometryCorrection odometryCorrection = new OdometryCorrection(odometer);
		odometer.odoCorrection = odometryCorrection;
		
		
		
		do {
			// clear the display
			t.clear();


			// ask the user whether the motors should drive with obstacles or not
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("  with | with no", 0, 1);
			t.drawString(" obst- | obst-  ", 0, 2);
			t.drawString(" acles | acles  ", 0, 3);
			t.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();

			odometer.start();
			odometryDisplay.start();
			Driver driver = new Driver(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK, odometer);

			if (buttonChoice == Button.ID_LEFT) {
			}
			driver.start();
			
			//path 1
			driver.travelTo(0, 2);
			driver.travelTo(1, 1);
			driver.travelTo(2, 2);
			driver.travelTo(2, 1);
			driver.travelTo(1, 0);
			
			//path 2
//			driver.travelTo(1, 1);
//			driver.travelTo(0, 2);
//			driver.travelTo(2, 2);
//			driver.travelTo(2, 1);
//			driver.travelTo(1, 0);
			
			//path 3
//			driver.travelTo(1, 0);
//			driver.travelTo(2, 1);
//			driver.travelTo(2, 2);
//			driver.travelTo(0, 2);
//			driver.travelTo(1, 1);
			
			//path 4
//			driver.travelTo(0, 1);
//			driver.travelTo(1, 2);
//			driver.travelTo(1, 0);
//			driver.travelTo(2, 1);
//			driver.travelTo(2, 2);


			// spawn a new Thread to avoid SquareDriver.drive() from blocking
//			(new Thread() {
//				public void run() {
//					Driver.travelTo(1, 1);
//				}
//			}).start();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}
