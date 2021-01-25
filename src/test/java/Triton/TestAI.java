package Triton;

import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.AI.Formation;
import Triton.CoreModules.AI.GameStates;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.AI.Strategies.Attack.BasicAttack;
import Triton.CoreModules.AI.Strategies.Defense.BasicDefense;
import Triton.CoreModules.AI.Strategies.SeizeOpportunity.ForwardFilling;
import Triton.CoreModules.AI.Strategies.Strategies;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Coordinates.Vec2D;
import Triton.Misc.ModulePubSubSystem.Module;

public class TestAI implements Module {
    private final RobotList<Ally> allies;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private final Formation formation;
    private final Estimator estimator;
    private final GoalKeeping goalKeeping;

    public TestAI(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        formation = Formation.getInstance();
        estimator = new Estimator(allies, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, estimator);
    }

    @Override
    public void run() {
        try {
            while (true) {
                formation.defaultFormation(allies);
                keeper.getBall();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


