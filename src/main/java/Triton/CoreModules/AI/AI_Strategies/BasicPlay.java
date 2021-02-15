package Triton.CoreModules.AI.AI_Strategies;

import Triton.CoreModules.AI.AI_Tactics.AttackPlanA;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.AI_Tactics.Tactics;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassEstimator;
import Triton.CoreModules.AI.Estimators.PassFinder;
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
    private final GoalKeeping goalKeeping;


    private final GapFinder gapFinder;
    private final PassFinder passFinder;

    public BasicPlay(RobotList<Ally> allies, Ally keeper,
                     RobotList<Foe> foes, Ball ball,
                     GapFinder gapFinder, PassFinder passFinder) {
        super();
        this.allies = allies;
        this.foes = foes;
        this.keeper = keeper;
        this.ball = ball;
        this.gapFinder = gapFinder;
        this.passFinder = passFinder;

        basicEstimator = new BasicEstimator(allies, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, basicEstimator);

        // construct tactics
        attack = new AttackPlanA(allies, keeper, foes, ball);
        getBall = new FillGapGetBall(allies, keeper, foes, ball, gapFinder);
    }

    @Override
    public void play() {
        if (basicEstimator.isBallUnderOurCtrl()) {
            // play offensive
            System.out.println("Ready To Attack");
        } else {
            if(basicEstimator.getBallHolder() instanceof Foe) {
                // play defense
                System.out.println("Time To Defend");
            } else {
                // Try to get ball & command remainder free bots to seek advantageous positions
                getBall.exec();
            }
        }
    }



}
