package Triton.AI.Estimators;


import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;
import Triton.Objects.Foe;
import Triton.Objects.Robot;

/* provide misc estimations methods to give AI
 * situation awareness of everything happening
 * on the game field
 * */
public class Estimator {
    private final Robot[] allies;
    private final Robot[] foes;
    private final Ball ball;
    public Estimator(Ally[] allies, Foe[] foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
    }

    /*
     * return the reference of the robot currently holding/dribbling the ball,
     * if no robot is currently holding the ball, return null
     */
    Robot getBallHolder() {
        return null;
    }

    /*
     * under the premises a robot is holding the ball,
     * return the trajectory of its currently aiming
     */
    Vec2D getAimTrajectory() {
        return null;
    }



}
