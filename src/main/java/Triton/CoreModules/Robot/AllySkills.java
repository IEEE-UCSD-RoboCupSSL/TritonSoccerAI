package Triton.CoreModules.Robot;

import Triton.CoreModules.Ball.Ball;
import Triton.Misc.Coordinates.Vec2D;

public interface AllySkills {
    /*** primitive control methods ***/
    void autoCap();

    void stop();

    /**
     * @param loc player perspective, millimeter
     */
    void moveTo(Vec2D loc);
    // Note: (moveTo/At & spinTo/At] are mutually exclusive to [pathTo & rotateTo]

    /**
     * @param vel player perspective, vector with unit as percentage from -100 to 100
     */
    void moveAt(Vec2D vel);

    /**
     * @param angle player perspective, degrees, starting from y-axis, positive is counter clockwise
     */
    void spinTo(double angle);

    /**
     * @param angVel unit is percentage from -100 to 100, positive is counter clockwise
     */
    void spinAt(double angVel);

    // runs in the caller thread
    void kick(Vec2D kickVel);


    /*** advanced control methods with path avoiding obstacles ***/
    void strafeTo(Vec2D endPoint, double angle);

    void sprintTo(Vec2D endPoint);

    void sprintToAngle(Vec2D endPoint, double angle);

    void rotateTo(double angle);


    /*** Soccer Skills methods ***/
    void getBall(Ball ball);

    void passBall(Vec2D receiveLoc, double ETA); // ETA: estimated arrival time, unit: milliseconds

    // To-do later: public void chipBall(/* parabola */) ...
    void dribBallTo(Ball ball, Vec2D kickLoc);

    void receive(Ball ball, Vec2D receiveLoc);

    void intercept(Ball ball);


    /* getters */
    boolean isHoldingBall();

    double dispSinceHoldBall();

    boolean isMaxDispExceeded();

    boolean isPosArrived(Vec2D loc);

    boolean isDirAimed(double angle);
}
