/*
 * OdometryDisplay.java
 */

package ca.mcgill.ecse211.lab3;

import lejos.hardware.lcd.TextLCD;

public class OdometryDisplay extends Thread {
	private static final long DISPLAY_PERIOD = 250;
	private Odometer odometer;
	private TextLCD t;
	private Navigation driver;

	// constructor
	public OdometryDisplay(Odometer odometer, TextLCD t, Navigation driver) {
		this.odometer = odometer;
		this.driver = driver;
		this.t = t;
	}

	// run method (required for Thread)
	public void run() {
		long displayStart, displayEnd;
		double[] position = new double[3];

		// clear the display once
		t.clear();

		while (true) {
			displayStart = System.currentTimeMillis();
			t.clear();
			// clear the lines for displaying odometry information
			t.drawString("X:              ", 0, 0);
			t.drawString("Y:              ", 0, 1);
			t.drawString("T:              ", 0, 2);
//			t.drawString("mode: ", 0, 3);
			// t.drawString("light: ", 0, 3);
			t.drawString("targetX:", 0, 4);
			t.drawString("targetY:", 0, 5);
			t.drawString("distanceUS:", 0, 6);

			// get the odometry information
			odometer.getPosition(position, new boolean[] { true, true, true });

			// display odometry information
			for (int i = 0; i < 3; i++) {
				t.drawString(formattedDoubleToString(position[i], 2), 3, i);
			}

//			if (driver.getMode() != null) {
//				t.drawString(driver.getMode(), 6, 3);
//			}

			/** only keep for testing **/
			// t.drawString(Integer.toString(odometer.odoCorrection.getcolorID()), 8, 3);
			if (this.driver != null) {
				t.drawString(formattedDoubleToString(driver.targetX, 2), 8, 4);
				t.drawString(formattedDoubleToString(driver.targetY, 2), 8, 5);
				t.drawString(formattedDoubleToString(driver.readUSDistance(), 3), 11, 6);
//				if (driver.getMode() != null) {
//					t.drawString(driver.getMode(), 6, 3);
//				}
			}
			// throttle the OdometryDisplay
			displayEnd = System.currentTimeMillis();
			if (displayEnd - displayStart < DISPLAY_PERIOD) {
				try {
					Thread.sleep(DISPLAY_PERIOD - (displayEnd - displayStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that OdometryDisplay will be interrupted
					// by another thread
				}
			}
		}
	}

	private static String formattedDoubleToString(double x, int places) {
		String result = "";
		String stack = "";
		long t;

		// put in a minus sign as needed
		if (x < 0.0)
			result += "-";

		// put in a leading 0
		if (-1.0 < x && x < 1.0)
			result += "0";
		else {
			t = (long) x;
			if (t < 0)
				t = -t;

			while (t > 0) {
				stack = Long.toString(t % 10) + stack;
				t /= 10;
			}

			result += stack;
		}

		// put the decimal, if needed
		if (places > 0) {
			result += ".";

			// put the appropriate number of decimals
			for (int i = 0; i < places; i++) {
				x = Math.abs(x);
				x = x - Math.floor(x);
				x *= 10.0;
				result += Long.toString((long) x);
			}
		}

		return result;
	}

}
