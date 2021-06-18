package Triton.CoreModules.AI.AI_Strategies;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Tactics.AttackPlanSummer2021;
import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
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


    private final GapFinder gapFinder;
    private final PassFinder passFinder;

    public Summer2021Play(Config config, SoccerObjects soccerObjects, GapFinder gapFinder, PassFinder passFinder) {
        this(soccerObjects.fielders, soccerObjects.keeper, soccerObjects.foes, soccerObjects.ball,
                gapFinder, passFinder, config);
    }

    public Summer2021Play(RobotList<Ally> fielders, Ally keeper,
                          RobotList<Foe> foes, Ball ball,
                          GapFinder gapFinder, PassFinder passFinder, Config config) {
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
        attack = new AttackPlanSummer2021(fielders, keeper, foes, ball, gapFinder, passFinder, config);
        getBall = new FillGapGetBall(fielders, keeper, foes, ball, gapFinder, config);
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
                    currState = States.START;
                }
            }
            case ATTACK -> {
                if(!attack.exec()) {
                    currState = States.START;
                }
            }
        }



        goalKeeping.passiveGuarding();
    }



}
