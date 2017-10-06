/*SquareDriver.java */
package ca.mcgill.ecse211.lab4;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.Button;

public class Navigation extends Thread implements UltrasonicController {

	// global variables for the code
	private static int FORWARD_SPEED = Lab4.FORWARD_SPEED;
	private static int ROTATE_SPEED = Lab4.ROTATE_SPEED;
	private static double WHEEL_RADIUS = Lab4.WHEEL_RADIUS;
	private static double TILE_BASE = Lab4.TILE_BASE;
	private static double TRACK = Lab4.TRACK;
	private static double ERROR = Lab4.ERROR;

//	public enum Mode {
//		DRIVING, AVOIDING
//	}
//
//	private Mode mode;

	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;
	private EV3LargeRegulatedMotor eyesMotor;
	private Odometer odometer;

	private static final int FILTER_OUT = 20;
	private int distanceUS;
	private int filterControl = 0;

	boolean navigating;
	public double targetX;
	public double targetY;
	public double targetTheta;

	// Variables for the P-Controller
	private static final int bandCenter = 15; // Offset from the wall (cm)
	private static final int bandWidth = 2; // Error margin
	private boolean isWallFollowing = false;
	private double initialAngle;
	private static final int MOTOR_SPEED = 150;
	private static final int MAXCORRECTION = 50;
	int correction;
	int errorCalculated;
	int propConstant;
	// This variable is made public to be able to use it in the Lab3 class to
	// call travelTo in a for loop (Refer to the travelTo method below)
	public static int i = 0;

	
	
	
	// Constructor for DriveUS which takes into account the US sensor motor
	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3LargeRegulatedMotor eyesMotor, double leftRadius, double rightRadius, double TRACK, Odometer odometer) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.eyesMotor = eyesMotor;
		this.odometer = odometer;
		
		this.isWallFollowing = false;
		this.navigating = false;
	}

	
	// Drive is used as the base of this class and further implementations are added to it to avoid obstacles
	/**
	 * Method to make the robot drive a certain distance FORWARD(only)
	 */
	public void drive(double travelDist) {
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(3000);
		}

		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		// Both booleans were set to true here because we want the program to continue reading the code lines
		// when it is going from one point to another on the grid, this way we can collect the US sensor
		// distances while driving. Otherwise, the program would be blocked by this thread until it gets to one point
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), true);
	}

	
	
	
	// Filters are added for the US sensor values. The error calculations are done inside it in order to get proper
	// US distances used for these calculations
	@Override
	public void processUSData(int distance1) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).

		if (distance1 >= 150 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		}
		else if (distance1 >= 150) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distanceUS = distance1;
		}
		else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distanceUS = distance1;
		}

		// Calculate the error for the implementation of the P-Controller and used a proportionality constant
		errorCalculated = bandCenter - readUSDistance();
		propConstant = 15;
		correction = (int) (propConstant * (double) (Math.abs(errorCalculated)));
	}

	/**
	 * This method will do most of the work. It will take as input coordinates, and convert them to actual distances (1*TILE_BASE)
	 */
	public void travelTo(double x, double y) {
		double px = odometer.getX(); // previous x
		double py = odometer.getY(); // previous y
		// double pthetha = odometer.getTheta(); //previous theta IN RADIANS

		double travelX = x * TILE_BASE - px;
		double travelY = y * TILE_BASE - py;
		double travelTotal = Math.sqrt(Math.pow(travelX, 2) + Math.pow(travelY, 2));
		targetX = x * TILE_BASE;
		targetY = y * TILE_BASE;

		navigating = true;
		turnTo(absoluteAngle(travelX, travelY));
		drive(travelTotal);

		eyesMotor.setSpeed(100);
		
		// When calling travelTo, since both booleans were set to true for the rotation of the wheels, to avoid
		// the robot from rotating to a position then immediately evaluating its next rotation to go to the second
		// position, we block it from finishing the method travelTo unless both motors for the wheels stopped
		// rotating thus it can go forward and then rotate again to go to another point

		while ((rightMotor.isMoving() && leftMotor.isMoving())) {
			// While the robot is driving from one waypoint to another, we want to check if it gets too
			// close to an obstacle

			if (distanceUS < 15 && isWallFollowing == false) {
				initialAngle = odometer.getTheta(); // We record the angle at which it came across the obstacle met
				isWallFollowing = true;
				leftMotor.stop(true); // The motors are stopped and the robot spins to start "wallfollowing"
				rightMotor.stop(true); // And its sensor also moves towards the wall.
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				leftMotor.rotate(convertAngle(WHEEL_RADIUS, TRACK, 80), true);
				rightMotor.rotate(-convertAngle(WHEEL_RADIUS, TRACK, 80), false);
				eyesMotor.rotate(55);
			}

			if (isWallFollowing) {

				// PCONTROLLER IMPLEMENTATION, taken from Laboratory 1:Wall Following

				// if error is insignificant
				if (Math.abs(errorCalculated) <= bandWidth) {
					leftMotor.setSpeed(MOTOR_SPEED);
					rightMotor.setSpeed(MOTOR_SPEED);
					leftMotor.forward();
					rightMotor.forward();
				}

				// too far
				else if (errorCalculated < 0) {

					correction = (int) (propConstant * (double) (Math.abs(errorCalculated)));
					if (correction >= MAXCORRECTION) {
						correction = MAXCORRECTION;
					}
					leftMotor.setSpeed((MOTOR_SPEED - correction));
					rightMotor.setSpeed(MOTOR_SPEED + correction);
					leftMotor.forward();
					rightMotor.forward();
				}

				// too close
				else if (errorCalculated > 0) {
					if (distanceUS < 13) {
						leftMotor.setSpeed(MOTOR_SPEED - (correction));
						rightMotor.setSpeed(MOTOR_SPEED + correction);
						leftMotor.backward();
						rightMotor.backward();

					}
					else {
						if (correction >= MAXCORRECTION) {
							correction = MAXCORRECTION;
						}
						leftMotor.setSpeed(MOTOR_SPEED + (correction));
						rightMotor.setSpeed(MOTOR_SPEED - correction);
						leftMotor.forward();
						rightMotor.forward();
					}
				}

			}

			// If it is "wallfollowing" and the difference between the angle it was when coming across the
			// obstacle and its actual angle is around 90 degrees then it should stop, set wallfollowing to false,
			// and rotate back its sensor to the front

			if (isWallFollowing && distance(odometer.getTheta(), initialAngle) < 18) {
				eyesMotor.rotate(-55);
				leftMotor.stop();
				rightMotor.stop();
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				isWallFollowing = false;

				// The public variable i is decremented, this way when the for loop is calling travelto again,
				// it calls for the same point so that the robot can continue going where it was going and not
				// start driving to the next point after obstacle
				i--;
				// Return is used to exit the travelTo method when the robot has avoided an obstacle, then it
				// is called again on the point where it was called to before starting to avoid an obstacle
				return;
			}

		}

		navigating = false;
	}

	/**
	 * This method, given distances to travel to, returns the absolute angle of the location relative to the location of the robot
	 */
	private double absoluteAngle(double travelX, double travelY) {
		double angle = 0;
		if (travelX + ERROR > 0) { // account for some error
			angle = Math.toDegrees(Math.atan(Math.abs(travelX) / Math.abs(travelY)));
			if (travelY < 0) {
				angle = 180 - angle;
			}
		}
		else {
			angle = Math.toDegrees(Math.atan(Math.abs(travelX) / Math.abs(travelY)));
			if (travelY < 0) {
				angle = 180 + angle;
			}
			else {
				angle = 360 - angle;
			}
		}
		return angle;
	}

	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double TRACK, double angle) {
		return convertDistance(radius, Math.PI * TRACK * angle / 360.0);
	}

	/**
	 * given an absolute theta, turn to that angle
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

	/**
	 * Length (angular) of a shortest way between two angles. It will be in range [0, 180]. taken from https://stackoverflow.com/questions/7570808/how-do-i-calculate-the-difference-of-two-angle-measures
	 */
	private double distance(double alpha, double beta) {
		double phi = Math.abs(beta - alpha) % 360; // This is either the distance or 360 - distance
		double distance = phi > 180 ? 360 - phi : phi;
		return distance;
	}

	public boolean isNavigating() {
		return navigating;
	}
	
	// This method is used to be able to read the ultrasonic sensor distances
	@Override
	public int readUSDistance() {
		return this.distanceUS;
	}
	
	
//	public String getMode() {
//		if (this.mode == Mode.DRIVING) {
//			return "DRIVING";
//		}
//		else if (this.mode == Mode.AVOIDING) {
//			return "AVOIDING";
//		}
//		return null;
//	}

}
