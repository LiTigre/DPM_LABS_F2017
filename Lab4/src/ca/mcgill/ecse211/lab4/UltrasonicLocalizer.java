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

	private static int ACCELERATION = Lab4.ACCELERATION;
	

	private Odometer odometer;
	private Navigation navi;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	static double threshHold = 40.0; // d
	static double noiseMargin = 1.2; // k

	// variables for the usSensor
	private static final int FILTER_OUT = 20;
	public int distanceUS;
	private int filterControl = 0;
	
	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
		this.navi = new Navigation(this.leftMotor, this.rightMotor, this.odometer);


	}
	
	public void risingEdge() {
		
		
		while (readUSDistance() < threshHold - noiseMargin) {
			navi.rotateClockwise();
		}
		double a = odometer.getTheta();
		
		while (readUSDistance() < threshHold + noiseMargin) {
			navi.rotateClockwise();

		}
		double b = odometer.getTheta();
		double fallingA = (a+b)/2.0;

		
		navi.stopMotors();
		
		
		while (readUSDistance() > threshHold - noiseMargin ) {
			navi.rotateCounterclockwise();
		}
		while (readUSDistance() < threshHold - noiseMargin ) {
			navi.rotateCounterclockwise();

		}
		a = odometer.getTheta();

		while (readUSDistance() < threshHold + noiseMargin ) {
			navi.rotateCounterclockwise();

		}
		b = odometer.getTheta();
		
		navi.stopMotors();
		
		double fallingB = (a+b)/2.0;

		double dTheta;
		if ( fallingA<fallingB ) { 
			dTheta = 45 - (( fallingA+fallingB )/2.0);
		}
		else { 
			dTheta = 225 - (( fallingA+fallingB )/2.0);
		} 
		double odTheta = odometer.getTheta();

		double theta = odTheta + dTheta;

		theta = (((int)(Math.abs(theta)*100)) % 36000) / 100.00;
		
		odometer.setTheta((theta*Math.PI)/180.0);
		
		navi.turnTo(0);
		
	}
	

	/**
	 * falling edge means that it goes from open -> wall
	 */
	public void fallingEdge() {
		
		while (readUSDistance() > threshHold + noiseMargin ) {
			navi.rotateCounterclockwise();
		}
		double a = odometer.getTheta();
		while (readUSDistance() > threshHold - noiseMargin ) {
			navi.rotateCounterclockwise();
		}
		double b = odometer.getTheta();

		double fallingA = (a+b)/2.0;

		
		navi.stopMotors();
		
		while (readUSDistance() < threshHold + noiseMargin ) {
			navi.rotateClockwise();
		}
		while (readUSDistance() > threshHold + noiseMargin ) {
			navi.rotateClockwise();
		}
		a = odometer.getTheta();
		while (readUSDistance() > threshHold - noiseMargin) {
			navi.rotateClockwise();
		}
		b = odometer.getTheta();

		navi.stopMotors();
		
		double fallingB = (a+b)/2.0;

		double dTheta;
		if ( fallingA<fallingB ) { 
			dTheta = 45 - (( fallingA+fallingB )/2.0);
		}
		else { 
			dTheta = 225 - (( fallingA+fallingB )/2.0);
		} 
		double odTheta = odometer.getTheta();
		double theta = odTheta + dTheta;
		theta = (((int)(Math.abs(theta)*100)) % 36000) / 100.00;
		odometer.setTheta((theta*Math.PI)/180.0);
		navi.turnTo(0);
		
		
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
