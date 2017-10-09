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
	private static int ACCELERATION = Lab4.ACCELERATION;

	public enum Edge {
		FALLING_EDGE, RISING_EDGE
	}

	private Edge edge;

	private Odometer odometer;
	private Navigation navi;
	
	private EV3LargeRegulatedMotor leftMotor;
	private EV3LargeRegulatedMotor rightMotor;

	static double threshHold = 40; // d
	static double noiseMargin = 1.0; // k

	// variables for the usSensor
	private static final int FILTER_OUT = 20;
	public int distanceUS;
	private int filterControl = 0;

	// TODO: create risingEdge()

	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer,
			Edge edge) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
		this.navi = new Navigation(this.leftMotor, this.rightMotor, this.odometer);

		this.edge = edge;
	}
	
	public UltrasonicLocalizer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, Odometer odometer) {
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);

	}
	
	public void risingEdge() {
		
		
		while (readUSDistance() < threshHold - noiseMargin) {
			navi.rotateCounterclockwise();
		}
		double a = odometer.getTheta();
		System.out.println("first a: " + a);
		
		while (readUSDistance() < threshHold + noiseMargin) {
			navi.rotateCounterclockwise();

		}
		double b = odometer.getTheta();
		System.out.println("first b: " + b);
		
		
		double fallingA = (a+b)/2.0;
		System.out.println("falling a: " + fallingA);

		
		rightMotor.stop(true);
		leftMotor.stop(false);
		
		
		while (readUSDistance() > threshHold - noiseMargin ) {
			navi.rotateClockwise();
		}
		while (readUSDistance() < threshHold - noiseMargin ) {
			navi.rotateClockwise();

		}
		a = odometer.getTheta();
		System.out.println("second a: " + a);

		while (readUSDistance() < threshHold + noiseMargin ) {
			navi.rotateClockwise();

		}
		b = odometer.getTheta();
		System.out.println("second b: " + b);
		
		rightMotor.stop(true);
		leftMotor.stop(false);
		
		double fallingB = (a+b)/2.0;
		System.out.println("falling b: " + fallingB);

		double dTheta;
		if ( fallingA<fallingB ) { 
			dTheta = 45 - (( fallingA+fallingB )/2.0);
		}
		else { 
			dTheta = 225 - (( fallingA+fallingB )/2.0);
		} 
		System.out.println("dtheta " + dTheta);
	
		double odTheta = odometer.getTheta();
		System.out.println("odometerTheta: " + odTheta);

		double theta = odTheta + dTheta;
		System.out.println("theta: " + theta);

		theta = (((int)(Math.abs(theta)*100)) % 36000) / 100;
		System.out.println("theta: " + theta);
		
		odometer.setTheta((theta*Math.PI)/180);
		System.out.println("odometerTheta: " + odometer.getTheta());
		
		navi.turnTo(0);
		
		System.out.println("final angle " + odometer.getTheta());
	}
	

	/**
	 * falling edge means that it goes from open -> wall
	 */
	public void fallingEdge() {
		
		while (readUSDistance() > threshHold + noiseMargin ) {
			navi.rotateCounterclockwise();
		}
		double a = odometer.getTheta();
		System.out.println("first a: " + a);
		while (readUSDistance() > threshHold - noiseMargin ) {
			navi.rotateCounterclockwise();
		}
		double b = odometer.getTheta();
		System.out.println("first b: " + b);

		double fallingA = (a+b)/2.0;
		System.out.println("falling a: " + fallingA);

		
		rightMotor.stop(true);
		leftMotor.stop(false);
		
		
		
		while (readUSDistance() < threshHold + noiseMargin ) {
			navi.rotateClockwise();

		}
		while (readUSDistance() > threshHold + noiseMargin ) {
			navi.rotateClockwise();

		}
		a = odometer.getTheta();
		System.out.println("second a: " + a);

		while (readUSDistance() > threshHold - noiseMargin) {
			navi.rotateClockwise();

		}
		
		b = odometer.getTheta();
		System.out.println("second b: " + b);

		rightMotor.stop(true);
		leftMotor.stop(false);
		
		double fallingB = (a+b)/2.0;
		System.out.println("falling b: " + fallingB);

		double dTheta;
		if ( fallingA<fallingB ) { 
			dTheta = 45 - (( fallingA+fallingB )/2.0);
		}
		else { 
			dTheta = 225 - (( fallingA+fallingB )/2.0);
		} 
		System.out.println("dtheta " + dTheta);
	
		double odTheta = odometer.getTheta();
		System.out.println("odometerTheta: " + odTheta);

		double theta = odTheta + dTheta;
		System.out.println("theta: " + theta);

		theta = (((int)(Math.abs(theta)*100)) % 36000) / 100;
		System.out.println("theta: " + theta);
		
		odometer.setTheta((theta*Math.PI)/180);
		System.out.println("odometerTheta: " + odometer.getTheta());
		
		navi.turnTo(0);
		
		System.out.println("final angle " + odometer.getTheta());
		
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
