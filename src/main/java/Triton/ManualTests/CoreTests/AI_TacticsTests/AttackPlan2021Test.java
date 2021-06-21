package Triton.ManualTests.CoreTests.AI_TacticsTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Tactics.AttackPlanSummer2021;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.TestUtil.TestUtil;
import Triton.ManualTests.TritonTestable;

import java.time.LocalDateTime;

import static Triton.Util.delay;

public class AttackPlan2021Test implements TritonTestable {
    RobotList<Triton.CoreModules.Robot.Ally.Ally> fielders;
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
