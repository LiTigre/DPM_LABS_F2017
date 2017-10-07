// Lab2.java

package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.lab4.Navigation;
import ca.mcgill.ecse211.lab4.UltrasonicPoller;
import ca.mcgill.ecse211.lab4.UltrasonicLocalizer.Edge;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab4 {

	static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	static final Port lightPort = LocalEV3.get().getPort("S2");
	static final Port usPort = LocalEV3.get().getPort("S3");

	static final double WHEEL_RADIUS = 2.15;
	static final double TRACK = 15.55;
	
	static final int FORWARD_SPEED = 250;
	static final int ROTATE_SPEED = 100;
	
	static final double TILE_BASE = 30.48;
	static final double ERROR = 3;
	
	static final int ACCELERATION = 3000;

	public static void main(String[] args) {

		int buttonChoice;

		
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[1];
		UltrasonicPoller usPoller = null;

		
		EV3ColorSensor colorSensor = new EV3ColorSensor(lightPort);

		
		final TextLCD t = LocalEV3.get().getTextLCD();

		
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		UltrasonicLocalizer usloca = new UltrasonicLocalizer(leftMotor, rightMotor, odometer, Edge.FALLING_EDGE);
		usPoller = new UltrasonicPoller(usDistance, usData, usloca);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t, usloca);
		usPoller.start();

		
		do {
			// clear the display
			t.clear();

			t.drawString("<               ", 0, 0);
			t.drawString("   please press ", 0, 1);
			t.drawString("     left to    ", 0, 2);
			t.drawString("     start      ", 0, 3);
			t.drawString("                ", 0, 4);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			odometer.start();
			odometryDisplay.start();
			
			
			usloca.fallingEdge();
			
			// Navigation driveUS = new Navigation(leftMotor, rightMotor, eyesMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK,
			// odometer);
			// usPoller = new UltrasonicPoller(usDistance, usData, driveUS);
			//
			// // start our odometer
			// odometer.start();
			// OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t, driveUS);
			//
			// odometryDisplay.start();
			// driveUS.start();
			// usPoller.start();
			//// int[][] wayPoints = { { 0, 2 }, { 1, 1 }, { 2, 2 }, { 2, 1 }, { 1, 0 } }; // path1
			// // int[][] wayPoints = { { 1, 1 }, { 0, 2 }, { 2, 2 }, { 2, 1 }, { 1, 0 } }; //path2
			// // int[][] wayPoints = { { 1, 0 }, { 2, 1 }, { 2, 2 }, { 0, 2 }, { 1, 1 } }; //path3
			// // int[][] wayPoints = { { 0, 1 }, { 1, 2 }, { 1, 0 }, { 2, 1 }, { 2, 2 } }; //path4
			//
			// int[][] wayPoints = { { 2, 1 }, { 1, 1 }, { 1, 2 }, { 2, 0 } }; //lab report path
			//
			//
			// for (Navigation.i = 0; Navigation.i < wayPoints.length; Navigation.i++) {
			// driveUS.travelTo(wayPoints[Navigation.i][0], wayPoints[Navigation.i][1]);
			// }

		}
		else{
			odometer.start();
			odometryDisplay.start();
			
			
			usloca.risingEdge();
		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}
