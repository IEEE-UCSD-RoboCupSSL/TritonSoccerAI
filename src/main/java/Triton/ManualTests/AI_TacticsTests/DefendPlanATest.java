package Triton.ManualTests.AI_TacticsTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TritonTestable;

public class DefendPlanATest implements TritonTestable {

    private DefendPlanA defendPlanA;
    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    public DefendPlanATest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
    }

    public boolean test(Config config) {

        defendPlanA = new DefendPlanA(fielders, keeper, foes, ball, 250, config);
        try {
            while (true) {
                defendPlanA.exec();
                Thread.sleep(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
