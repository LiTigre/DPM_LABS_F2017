/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.odometerlab;

import lejos.hardware.Sound;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final double TILE_BASE = 30.48;
	private Odometer odometer;
	public EV3ColorSensor colorSensor;
	public int colorID;
	public int xCounter;
	public int yCounter;
	public double theta1;
	private boolean firstX;
	private boolean firstY;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;
		colorSensor = new EV3ColorSensor(SensorPort.S1);
		Sound.setVolume(20);
		xCounter = 0;
		yCounter = 0;
		firstX = false;
		firstY = false;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		double theta;

		while (true) {
			correctionStart = System.currentTimeMillis();

			// TODO Place correction implementation here

			colorID = colorSensor.getColorID();

			if (colorID > 10) {

				theta = odometer.getTheta();

				// initial straight line going up the Y axis
				if (theta < (Math.PI / 4) || theta > (Math.PI * 7 / 4)) {
					if (firstY == false) { // crossing the line for the first time
						yCounter = 0;
						odometer.setY(yCounter * TILE_BASE);
						firstY = true;
					}
					else {
						yCounter++;
						odometer.setY(yCounter * TILE_BASE);
					}
				}

				// going along the X axis
				else if (theta > (Math.PI / 4) && theta < (Math.PI * 3 / 4)) {
					if (firstX == false) { // crossing the line for the first time
						xCounter = 0;
						odometer.setX(xCounter * TILE_BASE);
						firstX = true;
					}
					else {
						xCounter++;
						odometer.setX(xCounter * TILE_BASE);
					}
				}

				// down the y axis
				else if (theta > (Math.PI * 3 / 4) && theta < (Math.PI * 5 / 4)) {
					odometer.setY(yCounter * TILE_BASE);
					yCounter--;
				}

				// back towards the starting point, aka down the X axis
				else if (theta > (Math.PI * 5 / 4) && theta < (Math.PI * 7 / 4)) {
					odometer.setX(xCounter * TILE_BASE);
					xCounter--;
				}
				Sound.beep();

			}

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD - (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
}
