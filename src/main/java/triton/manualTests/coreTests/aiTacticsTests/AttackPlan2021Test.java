package triton.manualTests.coreTests.aiTacticsTests;

import triton.config.Config;
import triton.coreModules.ai.tactics.AttackPlanSummer2021;
import triton.coreModules.ai.estimators.AttackSupportMapModule;
import triton.coreModules.ai.estimators.PassProbMapModule;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.testUtil.TestUtil;
import triton.manualTests.TritonTestable;

import java.time.LocalDateTime;

import static triton.Util.delay;

public class AttackPlan2021Test implements TritonTestable {
    RobotList<triton.coreModules.robot.ally.Ally> fielders;
    Ally keeper;
    RobotList<Foe> foes;
    Ball ball;
    Config config;

    public AttackPlan2021Test(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball, Config config) {
        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
        this.config = config;

    }

    @Override
    public boolean test(Config config) {
        Ally ally = fielders.get(0);

        while (!ally.isHoldingBall()) {
            ally.getBall(ball);
            delay(3);
        }

        ally.stop();

        AttackSupportMapModule attackSupportMapModule = new AttackSupportMapModule(fielders, foes, ball);
        PassProbMapModule passProbMapModule = new PassProbMapModule(fielders, foes, ball);
        AttackPlanSummer2021 attackPlanSummer2021 = new AttackPlanSummer2021(fielders, keeper, foes, ball, attackSupportMapModule, passProbMapModule, config);
        AttackPlanSummer2021.States currState = attackPlanSummer2021.getCurrState();

        LocalDateTime twentySecsLater = LocalDateTime.now().plusSeconds(20);
        while(LocalDateTime.now().isBefore(twentySecsLater) && !currState.equals(AttackPlanSummer2021.States.Exit)) {

            attackPlanSummer2021.exec();
            currState = attackPlanSummer2021.getCurrState();
            delay(3);
        }

        attackPlanSummer2021.setCurrState(AttackPlanSummer2021.States.Start);

        TestUtil.enterKeyToContinue();
        return true;
    }
}
