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
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.Scanner;

public class CPassTest extends RobotSkillsTest {
    Scanner scanner;
    Ally __passer;
    Ally __receiver;
    RobotList<Ally> fielders;
    Ball ball;

    BasicEstimator basicEstimator;
    PassEstimatorMock passEstimator;

    public CPassTest(Scanner scanner, RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.scanner = scanner;
        this.__passer = fielders.get(0);
        this.__receiver = fielders.get(1);
        this.ball = ball;
        this.fielders = fielders;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passEstimator = new PassEstimatorMock();
    }

    @Override
    public boolean test() {

        boolean testRtn = true;
        try {
            __passer.kick(new Vec2D(2, 2));
            __receiver.kick(new Vec2D(2, 2));
            Thread.sleep(500);
            __passer.kick(new Vec2D(0, 0));
            __receiver.kick(new Vec2D(0, 0));




            Vec2D passingPos = new Vec2D(-1000, -1000);
            Vec2D secondReceivingPos = new Vec2D(-1500, 2000);

            Vec2D receivingPos = new Vec2D(1000, 1000);
            Vec2D secondPassingPos = new Vec2D(1500, 3000);


            Vec2D initPasserPos = passingPos.add(new Vec2D(0, 0));
            Vec2D initReceiverPos = receivingPos.add(new Vec2D(-1500, 0));

            /* preparation */
            passEstimator.setPassingPos(passingPos);
            passEstimator.setReceivingPos(receivingPos);
            passEstimator.setGoodTimeToKick(false);
            passEstimator.setBallArrivalETA(1.1);

            while (!__passer.isHoldingBall()) {
                while (!__passer.isHoldingBall()) {
                    __passer.getBall(ball);
                    __receiver.fastCurveTo(initReceiverPos);
                }
                while (!__passer.isPosArrived(initPasserPos) || !__receiver.isPosArrived(initReceiverPos)) {
                    __passer.fastCurveTo(initPasserPos);
                    __receiver.fastCurveTo(initReceiverPos);
                }
            }


            /* Begin test */
            PassState passState;
            Ally passer = null;
            Ally receiver = null;
            boolean toQuit = false;
            boolean firstTestCompleted = false;
            passEstimator.setOptimalReceiver(__receiver);
            while(!toQuit) {
                if(basicEstimator.isBallUnderOurCtrl()) {
                    if(CoordinatedPass.getPassState() == PassState.PENDING) {
                        if(firstTestCompleted) {
                            passEstimator.setOptimalReceiver(__passer);
                            passEstimator.setReceivingPos(secondReceivingPos);
                            passEstimator.setPassingPos(secondPassingPos);
                        }
                        passer = (Ally) basicEstimator.getBallHolder();
                        receiver = passEstimator.getOptimalReceiver();
                    }
                    passState = CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, passEstimator);
                    System.out.println(passState);
                    switch (passState) {
                        case PASSED -> {
                            if(passer == null) {
                                toQuit = true;
                            } else {
                                if (!firstTestCompleted) {
                                    passer.fastCurveTo(secondReceivingPos);
                                } else {
                                    passer.fastCurveTo(new Vec2D(0, 0));
                                }
                            }
                        }
                        case RECEIVE_SUCCESS -> {
                            if(!firstTestCompleted) {
                                firstTestCompleted = true;
                            } else {
                                toQuit = true;
                            }
                        }
                        case FAILED -> {
                            toQuit = true;
                            testRtn = false;
                        }
                    }

                    if(receiver != null) {
                        double dist = receivingPos.sub(receiver.getPos()).mag();
                        if (dist <= 500) {
                            passEstimator.setGoodTimeToKick(true);
                        }
                    }

                } else {
                    System.out.println("Ball Out of Our Control");
                }
            }

            fielders.stopAll();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return testRtn;
    }
}
