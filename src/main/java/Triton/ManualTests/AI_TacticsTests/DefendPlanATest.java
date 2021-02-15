package Triton.ManualTests.AI_TacticsTests;

import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;

import java.util.ArrayList;

import static Triton.PeriphModules.Display.PaintOption.*;

public class DefendPlanATest {

    private final DefendPlanA defendPlanA;

    public DefendPlanATest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        defendPlanA = new DefendPlanA(fielders, keeper, foes, ball);
    }

    public boolean test() {
        while(true) {
            defendPlanA.exec();
        }
    }
}
