package Triton.CoreModules.Robot;

import Triton.Misc.Coordinates.Vec2D;

import static Triton.CoreModules.Robot.AllyState.*;
import static Triton.CoreModules.Robot.AllyState.ROTATE;

public interface RobotSkills {
    /*** primitive control methods ***/
    public void autoCap();
    public void stop();
    /**
     * @param loc player perspective, millimeter
     */
    public void moveTo(Vec2D loc);
    // Note: (moveTo/At & spinTo/At] are mutually exclusive to [pathTo & rotateTo]
    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    public void moveAt(Vec2D vel);
    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    public void spinTo(double angle);
    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    public void spinAt(double angVel);
    // runs in the caller thread
    public void kick(Vec2D kickVel);


    /*** advanced control methods with path avoiding obstacles ***/
    public void strafeTo(Vec2D endPoint, double angle);
    public void sprintTo(Vec2D endPoint);
    public void sprintToAngle(Vec2D endPoint, double angle);
    public void rotateTo(double angle);


    /*** Soccer Skills methods ***/
    public void getBall();
    public void passBall(Vec2D receiveLoc, double ETA); // ETA: estimated arrival time, unit: milliseconds
    // To-do later: public void chipBall(/* parabola */) ...
    public void dribBallTo(Vec2D kickLoc);
    public void receiveBall(Vec2D receiveLoc);
    public void intercept();


    /* getters */
    public boolean isHoldingBall();
    public double netDispSinceHoldBall();
    public boolean isMaxDispExceeded();
    public boolean isLocArrived(Vec2D loc);
    public boolean isAngleAimed(double angle);
}
