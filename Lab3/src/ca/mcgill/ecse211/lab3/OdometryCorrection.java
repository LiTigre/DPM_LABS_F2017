/*
 * OdometryCorrection.java
 */
package ca.mcgill.ecse211.lab3;

import lejos.hardware.Sound;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3ColorSensor;

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final double TILE_BASE = 30.48;
	private Odometer odometer;
	
	private EV3ColorSensor colorSensor;
	private int prevColorID;	//average of 6 if on the woodboard
	private int colorID;
	
	private int xCounter;
	private int yCounter;
	public double theta1;

	// constructor
	public OdometryCorrection(Odometer odometer) {
		this.odometer = odometer;

		colorSensor = new EV3ColorSensor(SensorPort.S1);
		colorID = colorSensor.getColorID();
		prevColorID = colorSensor.getColorID();

		Sound.setVolume(20);

		xCounter = 0;
		yCounter = 0;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;
		double theta;

		while (true) {
			correctionStart = System.currentTimeMillis();

			colorID = colorSensor.getColorID();

			if (colorID > prevColorID + 4 && colorID != prevColorID) {

				theta = odometer.getTheta();

				// initial straight line going up the Y axis
				if (theta < (Math.PI / 4) || theta > (Math.PI * 7 / 4)) {
					odometer.setY(yCounter * TILE_BASE);
					yCounter++;
				}

				// going along the X axis
				else if (theta > (Math.PI / 4) && theta < (Math.PI * 3 / 4)) {
					odometer.setX(xCounter * TILE_BASE);
					xCounter++;
				}

				// down the y axis
				else if (theta > (Math.PI * 3 / 4) && theta < (Math.PI * 5 / 4)) {
					yCounter--;
					odometer.setY(yCounter * TILE_BASE);
				}

				// back towards the starting point, aka down the X axis
				else if (theta > (Math.PI * 5 / 4) && theta < (Math.PI * 7 / 4)) {
					xCounter--;
					odometer.setX(xCounter * TILE_BASE);
				}
				Sound.beep();
			}
			
			prevColorID = colorID;


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
	
	public int getxCounter(){
		return xCounter;
	}
	
	public int getyCounter(){
		return yCounter;
	}
	
	public int getcolorID(){
		return colorID;
	}
	
}
