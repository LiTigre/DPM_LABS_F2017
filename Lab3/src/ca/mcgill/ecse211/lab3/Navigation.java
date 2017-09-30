package ca.mcgill.ecse211.lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private double WHEEL_RADIUS;
	private double TRACK;
	private static final double TILE_BASE = 30.48;
	private static final double ERROR = 3;


	public EV3LargeRegulatedMotor leftMotor;
	public EV3LargeRegulatedMotor rightMotor;
	
	private Odometer odometer;
	private boolean navigating;

	
	public Navigation(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.TRACK = Lab3.TRACK;
		this.WHEEL_RADIUS = Lab3.WHEEL_RADIUS;
		this.navigating = false;
	}

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
		
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), false);

	}

	/**
	 * This method will do most of the work.
	 * It will take as input coordinants, and convert them to actual distances (1*TILE_BASE)
	 */
	public void travelTo(double x, double y) {
		navigating = true;

		double px = odometer.getX(); // previous x
		double py = odometer.getY(); // previous y

		double travelX = x*TILE_BASE - px;
		double travelY = y*TILE_BASE - py;
		double travelTotal = Math.sqrt(Math.pow(travelX, 2) + Math.pow(travelY,  2));	//pythagore
		
		turnTo(absoluteAngle(travelX, travelY));
		drive(travelTotal);
		
		navigating = false;
	}

	
	/**
	 * This method, given distances to travel to, returns the absolute angle of the location
	 * relative to the location of the robot
	 */
	private double absoluteAngle(double travelX, double travelY) {
		double angle = 0;
		if (travelX + ERROR > 0) {	//account for some error
			angle = Math.toDegrees(Math.atan(Math.abs(travelX)/Math.abs(travelY)));
			if (travelY < 0){
				angle = 180-angle;
			}
		}
		else {
			angle = Math.toDegrees(Math.atan(Math.abs(travelX)/Math.abs(travelY)));
			if (travelY < 0){
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
	public void turnTo(double theta) {		//ABSOLUTE angle
		double currentTheta = odometer.getTheta(); //theta of the robot in DEGREES
		double turnTheta = distance(currentTheta, theta);
		boolean crossover = false;		
		boolean clockwise = false;
		
		double range = currentTheta + 180;
		if (range > 360) {
			range = range - 360;
			crossover = true;	//range passes over 0 degrees
		}
		
		if (crossover) {	//crossover -> currentTheta between 180 and 360
			if (theta > currentTheta || theta < range) {	//in the 180 degrees counterclockwise
				clockwise = true;
			}
		}
		else {	//currentTheta between 0 and 180
			if (theta < range && theta > currentTheta) {	//in the 180 degrees clockwise
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
	
	public boolean isNavigating(){
		return navigating;
	}
	
	/**
     * Length (angular) of a shortest way between two angles.
     * It will be in range [0, 180].
     * taken from https://stackoverflow.com/questions/7570808/how-do-i-calculate-the-difference-of-two-angle-measures
     */
    private double distance(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360;       // This is either the distance or 360 - distance
        double distance = phi > 180 ? 360 - phi : phi;
        return distance;
    }
	

}