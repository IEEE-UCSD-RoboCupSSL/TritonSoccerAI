package Triton.CoreModules.AI.AI_Strategies;

import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;

public class BasicPlay extends Strategies{

    private final RobotList<Ally> allies;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;
    private final Estimator estimator;
    private final GoalKeeping goalKeeping;

    public BasicPlay(RobotList<Ally> allies, Ally keeper,
                     RobotList<Foe> foes, Ball ball) {
        super();
        this.allies = allies;
        this.foes = foes;
        this.keeper = keeper;
        this.ball = ball;

        estimator = new Estimator(allies, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, estimator);

        // construct tactics
        // ...
    }

    @Override
    public void play() {
        if (estimator.isBallUnderOurCtrl()) {
            // play offensive
            attack();
        } else {
            if(estimator.isBallWithinOurReach()) {
                // Try to get ball & command remainder free bots to seek advantageous positions
                getBallAndMoveRemainderBots();
            }
            else {
                // play defense
                defend();
            }
        }
    }

    protected void attack() {

    }

    protected void defend() {

    }

    protected void getBallAndMoveRemainderBots() {

    }


}
