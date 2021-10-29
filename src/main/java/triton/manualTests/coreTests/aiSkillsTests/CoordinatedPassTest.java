package triton.manualTests.coreTests.aiSkillsTests;

import triton.config.Config;
import triton.coreModules.ai.skills.CoordinatedPass;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.dijkstra.Pdg;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.coreTests.robotSkillsTests.RobotSkillsTest;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static triton.Util.delay;

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
            receiver.curveTo(new Vec2D(-1000, 3000));
            delay(1);
        }
        passer.stop();
        receiver.stop();
        // delay(500);


        Vec2D passPoint = new Vec2D(passer.getPos().add(new Vec2D(-500, -500)));
        Vec2D receptionPoint = new Vec2D(-2000, 3000);
        double passDir = receptionPoint.sub(passPoint).toPlayerAngle();
        double receiveDir = passPoint.sub(receptionPoint).toPlayerAngle();
        Vec2D kickVec = new Vec2D(2, 1);

        ArrayList<Pdg.Node> attackerNodes = new ArrayList<>();
        Pdg.AllyPassNode node1 = new Pdg.AllyPassNode(passer);
        Pdg.AllyRecepNode node2 = new Pdg.AllyRecepNode(receiver);
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
            CoordinatedPass cp = new CoordinatedPass((Pdg.AllyPassNode) attackerNodes.get(0),
                    (Pdg.AllyRecepNode) attackerNodes.get(1), ball, basicEstimator);
            try {
                while (passResult == CoordinatedPass.PassShootResult.Executing) {
                    passResult = cp.execute();
                    for (int i = 2; i < attackerNodes.size(); i++) {
                        if(attackerNodes.get(i) instanceof Pdg.AllyRecepNode) {
                            Pdg.AllyRecepNode recepNode = ((Pdg.AllyRecepNode) attackerNodes.get(i));
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
                    delay(2000);
                    return true;
                }
                case fail -> {
                    System.out.println("Failed");
                    passer.stop();
                    receiver.stop();
                    delay(2000);
                    return false;
                }
            }
        }
    }
}
