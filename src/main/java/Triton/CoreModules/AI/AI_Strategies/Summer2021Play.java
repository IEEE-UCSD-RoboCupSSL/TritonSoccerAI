package Triton.CoreModules.AI.AI_Strategies;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Tactics.AttackPlanSummer2021;
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

public class Summer2021Play extends Strategies {

    private States currState = States.START;

    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;
    private final BasicEstimator basicEstimator;
    private final GoalKeeping goalKeeping;


    private final AttackSupportMapModule atkSupportMap;
    private final PassProbMapModule passProbMap;

    public Summer2021Play(Config config, SoccerObjects soccerObjects, AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap) {
        this(soccerObjects.fielders, soccerObjects.keeper, soccerObjects.foes, soccerObjects.ball,
                atkSupportMap, passProbMap, config);
    }

    public Summer2021Play(RobotList<Ally> fielders, Ally keeper,
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
        attack = new AttackPlanSummer2021(fielders, keeper, foes, ball, atkSupportMap, passProbMap, config);
        getBall = new FillGapGetBall(fielders, keeper, foes, ball, atkSupportMap, config);
        defend = new DefendPlanA(fielders, keeper, foes, ball, 300, config);
    }

    public States getCurrState() {
        return currState;
    }

    public static enum States {
        START,
        DEFEND,
        ATTACK,
        GETBALL,
    }


    @Override
    public void play() {

        switch(currState) {
            case START -> {
                if(basicEstimator.isAllyHavingTheBall()) {
                    currState = States.ATTACK;
                } else {
                    if(basicEstimator.isBallWithinOurReach()) {
                        currState = States.GETBALL;
                    } else {
                        currState = States.DEFEND;
                    }
                }
            }
            case DEFEND -> {
                defend.exec();
                if(basicEstimator.isBallWithinOurReach()) {
                    currState = States.START;
                }
            }
            case GETBALL -> {
                getBall.exec();
                if(basicEstimator.isAllyHavingTheBall()) {
                    currState = States.ATTACK;
                }
            }
            case ATTACK -> {

                ((AttackPlanSummer2021)attack).setCurrState(AttackPlanSummer2021.States.Start);
                attack.exec();
                currState = States.START;
            }
        }



        goalKeeping.passiveGuarding();
    }



}
