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

	
	private Odometer odometer;

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
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	
	
	
	
	
	
	
	
	/**
	 * given an absolute theta, turn to that angle taken from our lab3
	 */
	public void turnTo(double theta) { // ABSOLUTE angle
		double currentTheta = odometer.getTheta(); // theta of the robot in DEGREES
		double turnTheta = distance(currentTheta, theta);
		boolean crossover = false;
		boolean clockwise = false;

		double range = currentTheta + 180;
		if (range > 360) {
			range = range - 360;
			crossover = true; // range passes over 0 degrees
		}

		if (crossover) { // crossover -> currentTheta between 180 and 360
			if (theta > currentTheta || theta < range) { // in the 180 degrees counterclockwise
				clockwise = true;
			}
		}
		else { // currentTheta between 0 and 180
			if (theta < range && theta > currentTheta) { // in the 180 degrees clockwise
				clockwise = true;
			}
		}

		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		if (clockwise) {
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, turnTheta), true);
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, turnTheta), false);
		}
		else {
			leftMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, turnTheta), true);
			rightMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, turnTheta), false);
		}

	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double TRACK, double angle) {
		return convertDistance(radius, Math.PI * TRACK * angle / 360.0);
	}

	/**
	 * Length (angular) of a shortest way between two angles. It will be in range [0, 180]. taken
	 * from
	 * https://stackoverflow.com/questions/7570808/how-do-i-calculate-the-difference-of-two-angle-measures
	 */
	private double distance(double alpha, double beta) {
		double phi = Math.abs(beta - alpha) % 360; // This is either the distance or 360 - distance
		double distance = phi > 180 ? 360 - phi : phi;
		return distance;
	}
	
	
	
	
	
	
	
	
	
}
