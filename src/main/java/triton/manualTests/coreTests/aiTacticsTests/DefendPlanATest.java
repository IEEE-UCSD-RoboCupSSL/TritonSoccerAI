package triton.manualTests.coreTests.aiTacticsTests;

import triton.config.Config;
import triton.coreModules.ai.tactics.DefendPlanA;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.TritonTestable;

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
