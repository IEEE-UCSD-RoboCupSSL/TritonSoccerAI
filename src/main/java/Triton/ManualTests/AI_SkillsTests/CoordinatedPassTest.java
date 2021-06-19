package Triton.ManualTests.AI_SkillsTests;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.DEPRECATED_CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.AI.AI_Tactics.AttackPlanSummer2021;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.AI.TritonProbDijkstra.PUAG;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static Triton.Util.delay;

public class CoordinatedPassTest extends RobotSkillsTest {
    Scanner scanner;
    Ally passer;
    Ally receiver;
    RobotList<Ally> fielders;
    Ball ball;
    BasicEstimator basicEstimator;

    public CoordinatedPassTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.ball = ball;
        this.fielders = fielders;
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passer = fielders.get(3);
        receiver = fielders.get(1);
    }

    @Override
    public boolean test(Config config) {

        while(!passer.isHoldingBall()) {
            passer.getBall(ball);
            receiver.curveTo(new Vec2D(1000, 3000));
            delay(1);
        }
        passer.stop();
        receiver.stop();
        delay(500);


        Vec2D passPoint = new Vec2D(passer.getPos().add(new Vec2D(-500, -500)));
        Vec2D receptionPoint = new Vec2D(2000, 3000);
        double passDir = receptionPoint.sub(passPoint).toPlayerAngle();
        double receiveDir = passPoint.sub(receptionPoint).toPlayerAngle();
        Vec2D kickVec = new Vec2D(3, 2);

        ArrayList<PUAG.Node> attackerNodes = new ArrayList<>();
        PUAG.AllyPassNode node1 = new PUAG.AllyPassNode(passer);
        PUAG.AllyRecepNode node2 = new PUAG.AllyRecepNode(receiver);
        node1.setPassPoint(passPoint);
        node1.setAngle(passDir);
        node1.setKickVec(kickVec);
        node2.setReceptionPoint(receptionPoint);
        node2.setAngle(receiveDir);

        attackerNodes.add(node1);
        attackerNodes.add(node2);


        while(true) {
            delay(2);

            /* Pass to Next */
            CoordinatedPass.PassShootResult passResult = CoordinatedPass.PassShootResult.Executing;
            CoordinatedPass cp = new CoordinatedPass((PUAG.AllyPassNode) attackerNodes.get(0),
                    (PUAG.AllyRecepNode) attackerNodes.get(1), ball, basicEstimator);
            try {
                while (passResult == CoordinatedPass.PassShootResult.Executing) {
                    passResult = cp.execute();
                    for (int i = 2; i < attackerNodes.size(); i++) {
                        if(attackerNodes.get(i) instanceof PUAG.AllyRecepNode) {
                            PUAG.AllyRecepNode recepNode = ((PUAG.AllyRecepNode) attackerNodes.get(i));
                            recepNode.getBot().curveTo(recepNode.getReceptionPoint(), recepNode.getAngle());
                        }
                    }
                    delay(3);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            switch (passResult) {
                case success -> {
                    System.out.println("Success!");
                    passer.stop();
                    receiver.stop();
                    return true;
                }
                case fail -> {
                    System.out.println("Failed");
                    passer.stop();
                    receiver.stop();
                    return false;
                }
            }
        }

    }
}
