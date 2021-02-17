package Triton.CoreModules.AI.AI_Strategies;

import Triton.CoreModules.AI.AI_Tactics.AttackPlanA;
import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;

public class BasicPlay extends Strategies {

    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;
    private final BasicEstimator basicEstimator;
    private final GoalKeeping goalKeeping;


    private final GapFinder gapFinder;
    private final PassFinder passFinder;

    public BasicPlay(RobotList<Ally> fielders, Ally keeper,
                     RobotList<Foe> foes, Ball ball,
                     GapFinder gapFinder, PassFinder passFinder) {
        super();
        this.fielders = fielders;
        this.foes = foes;
        this.keeper = keeper;
        this.ball = ball;
        this.gapFinder = gapFinder;
        this.passFinder = passFinder;
        gapFinder.run();
        passFinder.run();

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, basicEstimator);

        // construct tactics
        attack = new AttackPlanA(fielders, keeper, foes, ball, gapFinder, passFinder);
        getBall = new FillGapGetBall(fielders, keeper, foes, ball, gapFinder);
        defend = new DefendPlanA(fielders, keeper, foes, ball, 300);
    }

    @Override
    public void play() {
        if (basicEstimator.isBallUnderOurCtrl()) {
            // play offensive
            // System.out.println("Ready To Attack");
            attack.exec();
        } else {
            if(basicEstimator.getBallHolder() instanceof Foe) {
                // play defense
                //System.out.println("Time To Defend");
                defend.exec();
            } else {
                // Try to get ball & command remainder free bots to seek advantageous positions
                getBall.exec();
            }
        }

        goalKeeping.passiveGuarding();
    }



}
