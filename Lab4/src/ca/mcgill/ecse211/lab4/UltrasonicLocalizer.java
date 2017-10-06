package ca.mcgill.ecse211.lab4;


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
 * then, you can choose the angle to between the angle that you enter the noise margin
 * and when you exit it(aka have an actual falling edge)
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

public class UltrasonicLocalizer {
	//TODO: create fallingEdge()
	//TODO: create risingEdge()
	
	
	
	
	
	
}
