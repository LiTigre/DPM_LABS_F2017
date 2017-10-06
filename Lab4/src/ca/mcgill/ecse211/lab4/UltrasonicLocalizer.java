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
 * 
 * 
 * @author LiTigre
 *
 */

public class UltrasonicLocalizer {

}
