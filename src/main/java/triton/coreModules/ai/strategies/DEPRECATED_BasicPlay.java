package triton.coreModules.ai.strategies;

import triton.config.Config;
import triton.coreModules.ai.tactics.DEPRECATED_AttackPlanA;
import triton.coreModules.ai.tactics.DefendPlanA;
import triton.coreModules.ai.tactics.FillGapGetBall;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ai.estimators.PassProbMapModule;
import triton.coreModules.ai.goalKeeping.GoalKeeping;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.SoccerObjects;

public class DEPRECATED_BasicPlay extends Strategies {

    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;
    private final BasicEstimator basicEstimator;
    private final GoalKeeping goalKeeping;


    private final AttackSupportMapModule atkSupportMap;
    private final PassProbMapModule passProbMap;

    public DEPRECATED_BasicPlay(Config config, SoccerObjects soccerObjects, AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap) {
        this(soccerObjects.fielders, soccerObjects.keeper, soccerObjects.foes, soccerObjects.ball,
                atkSupportMap, passProbMap, config);
    }

    public DEPRECATED_BasicPlay(RobotList<Ally> fielders, Ally keeper,
                                RobotList<Foe> foes, Ball ball,
                                AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap, Config config) {
        super();
        this.fielders = fielders;
        this.foes = foes;
        this.keeper = keeper;
        this.ball = ball;
        this.atkSupportMap = atkSupportMap;
        this.passProbMap = passProbMap;
        atkSupportMap.run();
        passProbMap.run();

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, basicEstimator);

        // construct tactics
        attack = new DEPRECATED_AttackPlanA(fielders, keeper, foes, ball, atkSupportMap, passProbMap, config);
        getBall = new FillGapGetBall(fielders, keeper, foes, ball, atkSupportMap, config);
        defend = new DefendPlanA(fielders, keeper, foes, ball, 300, config);
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
