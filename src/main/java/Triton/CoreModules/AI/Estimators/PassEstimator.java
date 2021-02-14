package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;

public class PassEstimator {

    private final RobotList<Ally> allies;
    private final RobotList<Foe> foes;
    private final Ball ball;
    private final Ally goalKeeper;

    public PassEstimator(RobotList<Ally> allies, Ally goalKeeper, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
        this.goalKeeper = goalKeeper;
    }


    /* Estimates for Coordinated Passing */

    public Vec2D getOptimalPassingPos(Ally passer) {

        return new Vec2D(0, 0);
    }

    public Ally getOptimalReceiver() {
        // getPos
        return null;
    }

    public Vec2D getOptimalReceivingPos(Ally receiver) {
        // getPos
        return new Vec2D(0, 0);
    }

    /* return true if slack time > 0 */
    public boolean isGoodTimeToKick() {
        return false;
    }

    public double getBallArrivalETA() {
        return 0;
    }

}
