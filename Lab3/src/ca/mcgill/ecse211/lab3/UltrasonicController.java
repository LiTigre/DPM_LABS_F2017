package ca.mcgill.ecse211.lab3;

public interface UltrasonicController {

  public void processUSData(int distance);

  public int readUSDistance();
}
