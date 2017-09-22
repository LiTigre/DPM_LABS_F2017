package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer extends Thread {
	// robot position

	private static final long ODOMETER_PERIOD = 25; /*
													 * odometer update period,
													 * in ms
													 */
	public static final double WHEEL_BASE = 16;	//TODO: check the actual width of our robot
	public static final double WHEEL_RADIUS = 2.1;	
	public OdometryCorrection odoCorrection;
	
	private double x;
	private double y;
	private double theta;
	private double distL, distR, deltaD, deltaT, dX, dY;
	private int leftMotorTachoCount;
	private int rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	public static int lastTachoL;
	public static int lastTachoR;
	public static int nowTachoL;
	public static int nowTachoR;

	private Object lock; /* lock object for mutual exclusion */

	// default constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0.0;
		this.y = 0.0;
		this.theta = 0.0;
//		this.leftMotorTachoCount = 0;
//		this.rightMotorTachoCount = 0;
		lastTachoL = 0;
		lastTachoR = 0;
		nowTachoL = 0;
		nowTachoR = 0;
		
		//this.odoCorrection = new OdometryCorrection(this);
		
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		

		while (true) {
			updateStart = System.currentTimeMillis();

			// TODO put (some of) your odometer code here

			nowTachoL = leftMotor.getTachoCount(); // get tacho counts
			nowTachoR = rightMotor.getTachoCount();
			
			/** tacho returns the rotation its done in degrees **/
			
			//2*pi*R
			distL = 3.14159 * WHEEL_RADIUS * (nowTachoL - lastTachoL) / 180;	//hence why we devide by 360 to get number of rotations
			distR = 3.14159 * WHEEL_RADIUS * (nowTachoR - lastTachoR) / 180;
			
			// displacements
			lastTachoL = nowTachoL;
			lastTachoR = nowTachoR;
			deltaD = 0.5 * (distL + distR);	//averages the distance the robot has done
			deltaT = (distL - distR) / WHEEL_BASE ;	

			synchronized (lock) {
				/**
				 * Don't use the variables x, y, or theta anywhere but here!
				 * Only update the values of x, y, and theta in this block. Do
				 * not perform complex math
				 * 
				 */
				
				theta += deltaT;
				dX = deltaD * Math.sin(theta);	// compute X componenet of displacement
				dY = deltaD * Math.cos(theta);	// compute Y component of displacement
				x = x + dX; // update estimates of X and Y position
				y = y + dY;
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		double angle = theta * (180/Math.PI);
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				//TODO: make an if statement for if its over 360 degrees
				if (angle > 360){
					angle = angle-360;
				}
				position[2] = angle;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount
	 *            the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount
	 *            the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;
		}
	}
}
