package Triton.ManualTests.AI_SkillsTests;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassEstimatorMock;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;
import Triton.Misc.Coordinates.Vec2D;

import java.util.Scanner;

import static Triton.CoreModules.AI.AI_Skills.PassState.PENDING;

public class CPassTest extends RobotSkillsTest {
    Scanner scanner;
    Ally passer;
    Ally receiver;
    Ball ball;

    BasicEstimator basicEstimator;
    PassEstimatorMock passEstimatorMock;

    public CPassTest(Scanner scanner, RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.scanner = scanner;
        this.passer = fielders.get(0);
        this.receiver = fielders.get(1);
        this.ball = ball;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passEstimatorMock = new PassEstimatorMock();
    }

    @Override
    public boolean test() {
        try {
            passer.kick(new Vec2D(2, 2));
            receiver.kick(new Vec2D(2, 2));

            Vec2D passingPos = new Vec2D(-1000, -1000);
            Vec2D receivingPos = new Vec2D(1000, 1000);
            Vec2D initPasserPos = passingPos.add(new Vec2D(0, 0));
            Vec2D initReceiverPos = receivingPos.add(new Vec2D(-1500, 0));

            passEstimatorMock.setPassingPos(passingPos);
            passEstimatorMock.setReceivingPos(receivingPos);
            passEstimatorMock.setOptimalReceiver(receiver);
            passEstimatorMock.setGoodTimeToPass(false);
            passEstimatorMock.setBallArrivalETA(0.8);

            passer.kick(new Vec2D(0, 0));

            while (!passer.isHoldingBall()) {
                while (!passer.isHoldingBall()) {
                    passer.getBall(ball);
                    receiver.sprintTo(initReceiverPos);
                }
                while (!passer.isPosArrived(initPasserPos) || !receiver.isPosArrived(initReceiverPos)) {
                    passer.sprintTo(initPasserPos);
                    receiver.sprintTo(initReceiverPos);
                }
            }

            receiver.kick(new Vec2D(0, 0));
            PassState passState = PENDING;
            CoordinatedPass.setPending();
            while (passState != PassState.RECEIVE_SUCCESS && passState != PassState.FAILED) {
                double dist = receivingPos.sub(receiver.getPos()).mag();
                if (dist <= 200)
                    passEstimatorMock.setGoodTimeToPass(true);

                passState = CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, passEstimatorMock);
                System.out.println(passState);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
