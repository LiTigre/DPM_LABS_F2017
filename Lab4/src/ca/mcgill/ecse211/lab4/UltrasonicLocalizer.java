package ca.mcgill.ecse211.lab4;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

/**
 * To localize accurately --> determine the initial orientation of the robot
 * 
 * Use ultrasonic sensor to measure the distance to the two walls nearest to robot
 * 
 * the local maximum is halfway between the intersections of the curve and the line y=d 
 * (the constant d is chosen, following experimentation)
 * 
 * knowing the heading of the robot when it drops below d and when it rises above d 
 * would let you know how far you are from the corner
 * 
 * but because of noise, we need a noise margin 
 * so if the distance falls below d+k, then you enter the noise margin 
 * however, only when the distance falls below d-k do you actually have a falling edge
 * 
 * then, you can choose the angle to between the angle that you enter the noise margin and 
 * when you exit it (aka have an actual falling edge)
 * 
 * two scenarios: 
 * 1. starts !facing the wall 
 * 		- detect falling edge, switch direction, then detect another falling edge 
 * 		- detect falling edge, continue, detect rising edge 
 * 2. starts facing the wall 
 * 		- rising edge, switch, rising edge 
 * 		- rising edge, continue, falling edge
 * 
 * #####be careful of wraparound for angles#####
 * 
 * a, and b being the angles at which you detect the wall 
 * if ( a < b ) { 
 * 		dTheta = 45 - (( a+b )/2) 
 * }
 * else { 
 * 		dTheta = 225 - (( a+b )/2) 
 * } 
 * dTheta = angle to add to the heading given by odometer
 * (setTheta = getTheta + dTheta)
 * 
 * @author LiTigre
 *
 */

public class UltrasonicLocalizer implements UltrasonicController {

	// global variables for the code
	private static int FORWARD_SPEED = Lab4.FORWARD_SPEED;
	private static int ROTATE_SPEED = Lab4.ROTATE_SPEED;
	private static double WHEEL_RADIUS = Lab4.WHEEL_RADIUS;
	private static double TILE_BASE = Lab4.TILE_BASE;
	private static double TRACK = Lab4.TRACK;

	private Odometer odometer;

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	static double threshHold = 40; // d
	static double noiseMargin = 1.5; // k

	// variables for the usSensor
	private static final int FILTER_OUT = 20;
	private int distanceUS;
	private int filterControl = 0;

	// TODO: create fallingEdge()
	// TODO: create risingEdge()

	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;

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

	// This method is used to be able to read the ultrasonic sensor distances
	@Override
	public int readUSDistance() {
		return this.distanceUS;
	}

	@Override
	public void processUSData(int distance) {
		if (distance >= 150 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		}
		else if (distance >= 150) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distanceUS = distance;
		}
		else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distanceUS = distance;
		}

	}

}
