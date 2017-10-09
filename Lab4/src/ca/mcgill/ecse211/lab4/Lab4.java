// Lab2.java

package ca.mcgill.ecse211.lab4;

import ca.mcgill.ecse211.lab4.Navigation;
import ca.mcgill.ecse211.lab4.UltrasonicPoller;
import ca.mcgill.ecse211.lab4.UltrasonicLocalizer.Edge;
import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class Lab4 {

	static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));

	static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));

	static final Port lightPort = LocalEV3.get().getPort("S2");
	static final Port usPort = LocalEV3.get().getPort("S3");

	static final double WHEEL_RADIUS = 2.15;
	static final double TRACK = 15.55;
	static final double SENSOR_DIST = 10; 	
	//TODO: ^^^ measure the distance between the middle of the robot and the back light sensor
	
	static final int FORWARD_SPEED = 250;
	static final int ROTATE_SPEED = 100;
	
	static final double TILE_BASE = 30.48;
	static final double ERROR = 3;
	
	static final int ACCELERATION = 3000;

	public static void main(String[] args) {

		int buttonChoice;

		
		@SuppressWarnings("resource")
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);
		SampleProvider usDistance = usSensor.getMode("Distance");
		float[] usData = new float[1];
		UltrasonicPoller usPoller = null;

		
		EV3ColorSensor colorSensor = new EV3ColorSensor(lightPort);

		
		final TextLCD t = LocalEV3.get().getTextLCD();

		
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		UltrasonicLocalizer usloca = new UltrasonicLocalizer(leftMotor, rightMotor, odometer);
		LightLocalizer lightloca = new LightLocalizer(odometer, leftMotor, rightMotor, colorSensor);
		usPoller = new UltrasonicPoller(usDistance, usData, usloca);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t, usloca);
		usPoller.start();

		
		do {
			// clear the display
			t.clear();

			t.drawString("<         |          >", 0, 0);
			t.drawString("  press   |   press   ", 0, 1);
			t.drawString(" left for | right for ", 0, 2);
			t.drawString("  rising  |  falling  ", 0, 3);
			t.drawString("   edge   |    edge   ", 0, 4);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_RIGHT) {
			odometer.start();
			odometryDisplay.start();
			
			
			usloca.fallingEdge();
		}
		else{
			odometer.start();
			odometryDisplay.start();
			
			
			usloca.risingEdge();
		}
		
		do {
			// clear the display
			t.clear();

			t.drawString("    Press the      ", 0, 0);
			t.drawString("    escape button  ", 0, 1);
			t.drawString(" |  to start       ", 0, 2);
			t.drawString(" |  lightLocalizer ", 0, 3);
			t.drawString(" V                 ", 0, 4);

			buttonChoice = Button.waitForAnyPress();

		} while (buttonChoice != Button.ID_ESCAPE);
		
		lightloca.localize();
		
		
		
		

		while (Button.waitForAnyPress() != Button.ID_ESCAPE)
			;
		System.exit(0);
	}
}
