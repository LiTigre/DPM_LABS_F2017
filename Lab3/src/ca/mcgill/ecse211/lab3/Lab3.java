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

	static final Port usPort = LocalEV3.get().getPort("S2");

	public static final double WHEEL_RADIUS = 2.15;
	public static final double TRACK = 15.55; // TODO: check the actual width of our robot
	public static final double TILE_BASE = 30.48;

	private static final int bandCenter = 20; // Offset from the wall (cm)
	private static final int bandWidth = 2; // Width of dead band (cm)

	public static void main(String[] args) throws InterruptedException {
		int buttonChoice;

		/** taken from Lab2 **/
		@SuppressWarnings("resource") // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort); // usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance"); // usDistance provides samples from
																	// this instance
		float[] usData = new float[usDistance.sampleSize()]; // usData is the buffer in which data are
																// returned

		// Setup Ultrasonic Poller // This thread samples the US and invokes
		UltrasonicPoller usPoller = null; // the selected controller on each cycle

		/** --------------- **/
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		final Odometer odometer = new Odometer(leftMotor, rightMotor);
		
		NavigationObstacles obstacleDriver = new NavigationObstacles(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK, odometer,
				usDistance);
		Navigation driver = new Navigation(leftMotor, rightMotor, odometer);
		
		OdometryDisplay odometryDisplayObstacle = new OdometryDisplay(odometer, t, obstacleDriver);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);


		int[][] travelPoints = { { 0, 2 }, { 1, 1 }, { 2, 2 }, { 2, 1 }, { 1, 0 } };	//path1
//		int[][] travelPoints = { { 1, 1 }, { 0, 2 }, { 2, 2 }, { 2, 1 }, { 1, 0 } };	//path2
//		int[][] travelPoints = { { 1, 0 }, { 2, 1 }, { 2, 2 }, { 0, 2 }, { 1, 1 } };	//path3
//		int[][] travelPoints = { { 0, 1 }, { 1, 2 }, { 1, 0 }, { 2, 1 }, { 2, 2 } };	//path4
		


		
		do {
			// clear the display
			t.clear();

			// ask the user whether the motors should drive with obstacles or not
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("  with | with NO", 0, 1);
			t.drawString(" obst- | obst-  ", 0, 2);
			t.drawString(" acles | acles  ", 0, 3);
			t.drawString("       |        ", 0, 4);

			buttonChoice = Button.waitForAnyPress();

			odometer.start();
			
			if (buttonChoice == Button.ID_RIGHT) {	//NO obstacles
				odometryDisplay.start();
				driver.start();
				for (int[] points: travelPoints) {
					driver.travelTo(points[0], points[1]);
					while (driver.isNavigating()) {
						Thread.sleep(200);
					}
				}
			}
			
			else if (buttonChoice == Button.ID_LEFT) {
				odometryDisplayObstacle.start();
				//TODO: add the code for obstacle avoidance
			}
			
			/** from this part to the next blue comment, this should go in buttonID left **/
//			Navigator obstacleDriver = new Navigator(leftMotor, rightMotor, WHEEL_RADIUS, WHEEL_RADIUS, TRACK, odometer,
//					usDistance);

//			if (buttonChoice == Button.ID_LEFT) {
				usPoller = new UltrasonicPoller(usDistance, usData, obstacleDriver);
				usPoller.start();

//			}

//			obstacleDriver.start();
//			obstacleDriver.run();

			


			for (int[] points : travelPoints) {
				obstacleDriver.travelTo(points[0], points[1]);
				obstacleDriver.navigating = true;
				obstacleDriver.run();
				while (obstacleDriver.isNavigating()) {
					Thread.sleep(200);
				}
			}
			
			/** ----------------------------------- **/

		}

		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}
