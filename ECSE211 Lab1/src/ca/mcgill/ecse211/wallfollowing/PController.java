package ca.mcgill.ecse211.wallfollowing;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PController implements UltrasonicController {

	/* Constants */
	private static final int MOTOR_SPEED = 200;
	private static final int FILTER_OUT = 20;
	private static final int MAXCORRECTION = 100;

	private final int bandCenter;
	private final int bandWidth;
	private int distance;
	private int filterControl;


	public PController(int bandCenter, int bandwidth) {
		this.bandCenter = bandCenter;
		this.bandWidth = bandwidth;
		this.filterControl = 0;

		WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED); // Initialize motor
															// rolling forward
		WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
		WallFollowingLab.leftMotor.forward();
		WallFollowingLab.rightMotor.forward();
	}

	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal.
		// (n.b. this was not included in the Bang-bang controller, but easily
		// could have).

		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		}
		else if (distance >= 255) {
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

		
		// TODO: process a movement based on the us distance passed in (P style)

		/**
		 * CONSIDER LEFT WHEEL AS INNER WHEEL, CLOSER TO THE WALL
		 **/

		// if negative -> too far
		// positive -> too close
		int errorCalculated = bandCenter - readUSDistance();

		// A proportional constant that we will be using to change the wheels'
		// speed
		int propConstant = 15;
		// Compare it to the given error threshold
		// If it is less than the threshold, move forward
		int correction = (int) (propConstant * (double) (Math.abs(errorCalculated)));
		//double ratio;
		

		// if error is insignificant
		if (Math.abs(errorCalculated) <= bandWidth) {
			WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED);
			WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED);
			WallFollowingLab.leftMotor.forward();
			WallFollowingLab.rightMotor.forward();
		}

		// too far
		else if (errorCalculated < 0) {
		/*	if (tightTurn) {
				WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED - correction );
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + correction); // Speed outer wheel
				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();
			}*/
			// {
				correction = (int) (propConstant * (double) (Math.abs(errorCalculated)));
				if (correction >= MAXCORRECTION) {
					correction = MAXCORRECTION;
				}
				WallFollowingLab.leftMotor.setSpeed( (MOTOR_SPEED -  correction));
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED + correction); // Speed
																		// outer
																		// wheel
				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();
			//}
		}

		// too close
		else if (errorCalculated > 0) {
			/*if (readUSDistance() < 12) {
				WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED-correction);
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED+ (correction));
				WallFollowingLab.leftMotor.backward();
				WallFollowingLab.rightMotor.backward();	
			} */
			
			//else {
				//correction = (int) (propConstant * (double) (Math.abs(errorCalculated)));
				if (correction >= MAXCORRECTION) {
					correction = MAXCORRECTION;
				}
				WallFollowingLab.leftMotor.setSpeed(MOTOR_SPEED + (correction));
				WallFollowingLab.rightMotor.setSpeed(MOTOR_SPEED - correction); 
				WallFollowingLab.leftMotor.forward();
				WallFollowingLab.rightMotor.forward();
			//}
		}

	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
