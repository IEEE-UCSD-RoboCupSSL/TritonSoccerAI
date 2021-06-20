package Triton.CoreModules.AI.AI_Strategies;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Tactics.DEPRECATED_AttackPlanA;
import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.SoccerObjects;

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
