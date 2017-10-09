package ca.mcgill.ecse211.lab4;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.sensor.EV3ColorSensor;

/**
 * For this part, the light sensor will be used to detect the gridlines
 * relative to the robot's position
 * 
 * We have to move the robot close enough to (0, 0) so that the robot
 * can pick up the lines
 * aka turn to 45 degrees and move forward
 * 
 * d = distance from center to light sensor
 * for y axis
 * 		x = -d cos(yTheta/2)
 * for x axis
 * 		y = -d cos(xTheta/2)
 * 
 * 
 * 
 * 
 * @author LiTigre
 *
 */

public class LightLocalizer {
	// global variables for the code
	private static int FORWARD_SPEED = Lab4.FORWARD_SPEED;
	private static int ROTATE_SPEED = Lab4.ROTATE_SPEED;
	private static double WHEEL_RADIUS = Lab4.WHEEL_RADIUS;
	private static double TILE_BASE = Lab4.TILE_BASE;
	private static double TRACK = Lab4.TRACK;
	private static int ACCELERATION = Lab4.ACCELERATION;
	private static double SENSOR_DIST = Lab4.SENSOR_DIST;

	
	private Odometer odometer;
	private Navigation navi;

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	public EV3ColorSensor colorSensor;
	public int colorID;

	public LightLocalizer(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3ColorSensor colorSensor) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.colorSensor = colorSensor;
		this.navi = new Navigation(this.leftMotor, this.rightMotor, this.odometer);
		
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	
	public void localize() {
		getReady();
		navi.turnTo(0);
		
	}
	
	/**
	 * this method will place our robot in position for the lightLocalizer
	 * make it face the corner, and then make it back up until it detects a line
	 * then back it up so that (0, 0) is between the sensor and the middle of the robot
	 */
	private void getReady() {
		navi.turnTo(225);
		while (colorID < 10) {
			navi.backup();
		}
		navi.drive(-(SENSOR_DIST/2));
	}
	
	
	
	
	
	
	
	
	
	
}
