package Triton.CoreModules.AI.AI_Strategies;

import Triton.CoreModules.AI.AI_Tactics.AttackPlanA;
import Triton.CoreModules.AI.AI_Tactics.Tactics;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class BasicPlay extends Strategies {

    private final RobotList<Ally> allies;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;
    private final BasicEstimator basicEstimator;
    private final PassEstimator passEstimator;
    private final GoalKeeping goalKeeping;
    private final Tactics attack;

    public BasicPlay(RobotList<Ally> allies, Ally keeper,
                     RobotList<Foe> foes, Ball ball) {
        super();
        this.allies = allies;
        this.foes = foes;
        this.keeper = keeper;
        this.ball = ball;

        basicEstimator = new BasicEstimator(allies, keeper, foes, ball);
        passEstimator = new PassEstimator(allies, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, basicEstimator);

        // construct tactics
        // ...
        attack = new AttackPlanA(allies, keeper, foes, ball, basicEstimator, passEstimator);
    }

    @Override
    public void play() {
        if (basicEstimator.isBallUnderOurCtrl()) {
            // play offensive
            offense();
        } else {
            if (basicEstimator.isBallWithinOurReach()) {
                // Try to get ball & command remainder free bots to seek advantageous positions
                getBallAndMoveRemainderBots();
            } else {
                // play defense
                defense();
            }
        }
    }

    protected void offense() {
        attack.exec();
    }

    protected void getBallAndMoveRemainderBots() {

    }

    protected void defense() {

    }


}
