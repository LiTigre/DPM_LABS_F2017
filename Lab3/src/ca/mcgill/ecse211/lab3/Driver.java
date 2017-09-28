/*
 * SquareDriver.java
 */
package ca.mcgill.ecse211.lab3;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Driver extends Thread {
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double WHEEL_RADIUS = 2.1;
	private static final double TILE_BASE = 30.48;
	private static final double ERROR = 0.1;

//	private double previousX;
//	private double previousY;

	public EV3LargeRegulatedMotor leftMotor;
	public EV3LargeRegulatedMotor rightMotor;
	
	private Odometer odometer;
	private double width;
//	public boolean driving;

	public Driver(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, double leftRadius,
			double rightRadius, double width, Odometer odometer) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.odometer = odometer;
		this.width = width;
//		previousX = 0;
//		previousY = 0;
//		driving = false;
	}

	private void drive(double travelDist) {
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(3000);
		}
		
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);

		
		leftMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), true);
		rightMotor.rotate(convertDistance(WHEEL_RADIUS, (int) (travelDist)), false);

	}

	public void travelTo(double x, double y) {
		double px = odometer.getX(); // previous x
		double py = odometer.getY(); // previous y
		// double pthetha = odometer.getTheta(); //previous theta IN RADIANS

		double travelX = x*TILE_BASE - px;
		double travelY = y*TILE_BASE - py;
		double travelTotal = Math.sqrt(Math.pow(travelX, 2) + Math.pow(travelY,  2));
		
		turnTo(absoluteAngle(travelX, travelY));
		drive(travelTotal);
		
//		previousX = x;
//		previousY = y;
	}

	
	private double absoluteAngle(double travelX, double travelY) {
		double angle = 0;
		if (travelX + ERROR > 0) {	//account for some error
			angle = Math.toDegrees(Math.atan(Math.abs(travelX)/Math.abs(travelY)));
			if (travelY < 0){
				angle = angle + 90;
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
		double pTheta = odometer.getTheta(); //previous angle IN DEGREES
		if (pTheta > 355 || pTheta < 5){
			pTheta = 0;
		}
		double turnTheta = theta - pTheta;
	
		
		
//		if (turnTheta > 360){
//			turnTheta = turnTheta - 360;
//		}
//		if (turnTheta < 0){
//			turnTheta = turnTheta + 360;
//		}
		
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);

		if (turnTheta > 180) {	//COUNTERclockwise
			leftMotor.rotate(-convertAngle(WHEEL_RADIUS, width, turnTheta), true);
			rightMotor.rotate(convertAngle(WHEEL_RADIUS, width, turnTheta), false);
		}
		else {					//clockwise
			leftMotor.rotate(convertAngle(WHEEL_RADIUS, width, turnTheta), true);
			rightMotor.rotate(-convertAngle(WHEEL_RADIUS, width, turnTheta), false);
		}
		
		
	}

}
