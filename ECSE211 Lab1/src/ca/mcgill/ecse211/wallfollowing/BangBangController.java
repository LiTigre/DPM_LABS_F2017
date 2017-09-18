package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.*;

public class BangBangController implements UltrasonicController {

	private static final int FILTER_OUT = 25;
	private static final int TURN_OUT = 65;

	private final int bandCenter;
	private final int bandwidth;
	private final int motorLow;
	private final int motorHigh;
	private int distance;
	private int filterControl;

	public boolean tightTurn;
	private int tightTurnControl;

	public BangBangController(int bandCenter, int bandwidth, int motorLow, int motorHigh) {
		// Default Constructor
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.motorLow = motorLow;
		this.motorHigh = motorHigh;
		this.filterControl = 0;
		this.tightTurnControl = 0;

		WallFollowingLab.leftMotor.setSpeed(motorHigh); // Start robot moving
														// forward
		WallFollowingLab.rightMotor.setSpeed(motorHigh);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();

	}

	@Override
	public void processUSData(int distance) {
		// this.distance = distance;
		// TODO: process a movement based on the us distance passed in
		// (BANG-BANG style)

		if (distance >= 100 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		}
		else if (distance >= 100) {
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

		if (distance >= 45 && tightTurnControl < TURN_OUT) {
			tightTurnControl++;
		}
		else if (distance >= 45) {
			// this.distance = distance;
			this.tightTurn = true;
		}
		else {
			tightTurnControl = 0;
			tightTurn = false;
			// this.distance = distance;

		}

		/**
		 * CONSIDER LEFT WHEEL AS INNER WHEEL, CLOSER TO THE WALL
		 **/

		// Let's create an error integer variable 
		int errorCalculated = bandCenter - readUSDistance();
		// Compare it to the given error threshold
		// If it is less than the threshold, move forward

		// error is insignificant
		if (Math.abs(errorCalculated) <= bandwidth) {
			WallFollowingLab.leftMotor.setSpeed(motorHigh);
			WallFollowingLab.rightMotor.setSpeed(motorHigh);
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
		}

		// If the error is negative, it is too far, slow inner wheel
		// Cannot speed up other wheel as only two speeds are given

		else if (errorCalculated < 0) {
			
			if (tightTurn) {
				WallFollowingLab.leftMotor.setSpeed(motorLow);
				WallFollowingLab.rightMotor.setSpeed(motorLow - 30);
				WallFollowingLab.leftMotor.backward();
				WallFollowingLab.rightMotor.forward();
			}
			else {
				WallFollowingLab.leftMotor.setSpeed(motorLow);
				WallFollowingLab.rightMotor.setSpeed(motorHigh + 30);
				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();
			}
		}

		// If the error is positive, it is too close, slow outer wheel
		else if (errorCalculated > 0) {
			if (readUSDistance() < 18) {
				WallFollowingLab.leftMotor.setSpeed((int) (motorLow + 20));
				WallFollowingLab.rightMotor.setSpeed((int) (1.8 * motorHigh));
				WallFollowingLab.leftMotor.backward();
				WallFollowingLab.rightMotor.backward();
			}

			else {
				WallFollowingLab.leftMotor.setSpeed(motorHigh);
				WallFollowingLab.rightMotor.setSpeed(motorLow);
				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();
			}
		}
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
