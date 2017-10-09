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
	private static int ACCELERATION = Lab4.ACCELERATION;
	private static double SENSOR_DIST = Lab4.SENSOR_DIST;

	
	private Odometer odometer;
	private Navigation navi;

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	public EV3ColorSensor colorSensor;
	public int colorID;
	
	private double[] thetas;

	public LightLocalizer(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3ColorSensor colorSensor) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.colorSensor = colorSensor;
		this.navi = new Navigation(this.leftMotor, this.rightMotor, this.odometer);
		this.thetas = new double[4];
		
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	
	public void localize() {
		getReady();
		navi.turnTo(0);
		detectLines();
		
		correction();
		navi.travelTo(0, 0);
		navi.turnTo(0);
	}
	
	/**
	 * this method will place our robot in position for the lightLocalizer
	 * make it face the corner, and then make it back up until it detects a line
	 * then back it up so that (0, 0) is between the sensor and the middle of the robot
	 */
	private void getReady() {
		while (colorID < 10) {
			colorID = colorSensor.getColorID();
			navi.forward();
		}
		Sound.beep();
		navi.stopMotors();
		navi.drive(-(SENSOR_DIST) * 1.8);
		colorID = colorSensor.getColorID();

		
		navi.turnTo(90);
		while (colorID < 10) {
			colorID = colorSensor.getColorID();
			navi.forward();
		}
		Sound.beep();
		navi.stopMotors();
		navi.drive(-(SENSOR_DIST) * 1.7);
		
	}
	
	//Counter clockwise so hits the y axis first
	private void detectLines() {
		navi.rotateCounterclockwise();
		int lineCount = 0;
		while (lineCount < 4) {
			colorID = colorSensor.getColorID();
			if (colorID > 10) {
				thetas[lineCount] = odometer.getTheta();
				Sound.beep();
				lineCount++;
			}
		}
		navi.stopMotors();
	}
	
	private void correction() {
		//thetas[0] and [2] -> y axis
		//thetas[2] and [3] -> x axis
		double xCorrection, yCorrection;
		double radianThetaY = ( navi.distance(thetas[0], thetas[2]) * Math.PI )/ 180;
		double radianThetaX = ( navi.distance(thetas[1], thetas[3]) * Math.PI )/ 180;
		xCorrection = -(SENSOR_DIST)*Math.cos(radianThetaY/2);
		yCorrection = -(SENSOR_DIST)*Math.cos(radianThetaX/2);
		odometer.setX(xCorrection);
		odometer.setY(yCorrection);
		
	}
	
	
	
	
	
	
	
}
