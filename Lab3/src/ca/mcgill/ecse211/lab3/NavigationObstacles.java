/*
 * SquareDriver.java
 */
package ca.mcgill.ecse211.lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;

public class NavigationObstacles extends Thread implements UltrasonicController {
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final int MOTOR_SPEED = 200;

	private static final double WHEEL_RADIUS = 2.15;
	private static final double TILE_BASE = 30.48;
	private static final double ERROR = 5;
	private static final int FILTER_OUT = 20;

	private int filterControl;
	int distance;
	boolean navigating;

	public enum Mode {
		Driving, Turning, Avoiding
	}

	private Mode mode;

	double targetX;
	double targetY;
	private double targetTheta;

	public EV3LargeRegulatedMotor leftMotor;
	public EV3LargeRegulatedMotor rightMotor;

	private Odometer odometer;
	private double width;

	public NavigationObstacles(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double leftRadius,
			double rightRadius, double width, Odometer odometer, SampleProvider usDistance) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.width = width;
		navigating = true;
		targetX = 0;
		targetY = 0;
		targetTheta = 0;
		this.mode = Mode.Driving;
	}

	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).

		if (distance >= 150 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		}
		else if (distance >= 150) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		}
		else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}

	}

	public void run() {

		while (true) {
			if (navigating) {
				if (!obstacle()) {
					switch (this.mode) {
					case Driving:
						//if the angle is bigger than 3 degrees difference
						if (!(distance(odometer.getTheta(), targetTheta) < 3)) {
							this.mode = Mode.Turning;
						}
						//correct location
						else if ((Math.abs(targetX - odometer.getX()) < ERROR) && 
								(Math.abs(targetY - odometer.getY()) < ERROR)) {
							this.mode = Mode.Turning;
						}
						else {
							drive();
						}
//						// if it's not at its correct location
//						else if (!(Math.abs(targetX - odometer.getX()) < ERROR)
//								|| !(Math.abs(targetY - odometer.getY()) < ERROR)) { // if not in bandwidth
//							drive();
//						}
//						else { // in correct location
//							this.mode = Mode.Turning;
//						}
						break;
					case Turning:
						if (!(distance(odometer.getTheta(), targetTheta) < 3)) {
							turnTo(absoluteAngle(targetX - odometer.getX(), targetY - odometer.getY()));
						}
						else {
							this.mode = Mode.Driving;
						}
						break;
					default:
						break;

					}
				}
				else {
					if (this.mode == Mode.Avoiding) {
						avoid();
						this.mode = Mode.Driving;
					}
				}

				

			}
			if (Math.abs(targetX - odometer.getX()) < ERROR && Math.abs(targetY - odometer.getY()) < ERROR
					&& Math.abs(targetTheta - odometer.getTheta()) < ERROR) {
				navigating = false;
			}
		}
	}

	private void avoid() {
		leftMotor.setSpeed(MOTOR_SPEED - (50));
		rightMotor.setSpeed(MOTOR_SPEED + 50);
		leftMotor.backward();
		rightMotor.backward();
	}

	private boolean obstacle() {
		if (readUSDistance() < 8) {
			this.mode = Mode.Avoiding;
			return true;
		}
		return false;
	}

	/**
	 * Method to make the robot drive a certain distance FORWARD(only)
	 */
	public void drive() {
//		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
//			motor.stop();
//			motor.setAcceleration(3000);
//		}

		leftMotor.setSpeed(FORWARD_SPEED-100);
		rightMotor.setSpeed(FORWARD_SPEED-100);
		leftMotor.forward();
		rightMotor.forward();

		// leftMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), true);
		// rightMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), false);

	}

	/**
	 * This method will do most of the work. It will take as input coordinants, and convert them to actual distances (1*TILE_BASE)
	 */
	public void travelTo(double x, double y) {
		targetX = x * TILE_BASE;
		targetY = y * TILE_BASE;
		targetTheta = absoluteAngle(x * TILE_BASE - odometer.getX(), y * TILE_BASE - odometer.getY());
		navigating = true;
		this.mode = Mode.Driving;

		// double px = odometer.getX(); // previous x
		// double py = odometer.getY(); // previous y
		// // double pthetha = odometer.getTheta(); //previous theta IN RADIANS
		//
		// double travelX = x*TILE_BASE - px;
		// double travelY = y*TILE_BASE - py;
		// double travelTotal = Math.sqrt(Math.pow(travelX, 2) + Math.pow(travelY, 2));
		//
		// turnTo(absoluteAngle(travelX, travelY));
		// drive(travelTotal);
		//
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

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
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

		leftMotor.setSpeed(ROTATE_SPEED - 100);
		rightMotor.setSpeed(ROTATE_SPEED - 100);

		if (clockwise) {
			leftMotor.forward();
			rightMotor.backward();
			// leftMotor.rotate(convertAngle(WHEEL_RADIUS, width, turnTheta), true);
			// rightMotor.rotate(-convertAngle(WHEEL_RADIUS, width, turnTheta), false);
		}
		else {
			leftMotor.backward();
			rightMotor.forward();
			// leftMotor.rotate(-convertAngle(WHEEL_RADIUS, width, turnTheta), true);
			// rightMotor.rotate(convertAngle(WHEEL_RADIUS, width, turnTheta), false);
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

	@Override
	public int readUSDistance() {
		// TODO Auto-generated method stub
		return this.distance;
	}

	public boolean isNavigating() {
		return navigating;
	}

	public String getMode() {
		if (this.mode == Mode.Driving) {
			return "Driving";
		}
		else if (this.mode == Mode.Turning) {
			return "Turning";
		}
		else if (this.mode == Mode.Avoiding) {
			return "Avoiding";
		}
		return null;
	}

}
