package Triton.ManualTests.AI_SkillsTests;

import Triton.CoreModules.AI.AI_Skills.CoordinatedPass;
import Triton.CoreModules.AI.AI_Skills.PassState;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.AI.Estimators.PassInfo;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.ManualTests.RobotSkillsTests.RobotSkillsTest;

import java.util.Scanner;

public class CPassTest extends RobotSkillsTest {
    Scanner scanner;
    Ally __passer;
    RobotList<Ally> fielders;
    Ball ball;

    BasicEstimator basicEstimator;
    PassFinder passFinder;
    PassInfo info;

    public CPassTest(Scanner scanner, RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.scanner = scanner;
        this.ball = ball;
        this.fielders = fielders;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passFinder = new PassFinder(fielders, foes, ball);
        passFinder.run();
    }

    @Override
    public boolean test() {

        boolean testRtn = true;
        try {
            /* preparation */
            __passer = fielders.get(0);
            while (!__passer.isHoldingBall()) {
                __passer.getBall(ball);
            }

            /* Begin test */
            PassState passState;
            Ally passer = null;
            Ally receiver = null;
            boolean toQuit = false;

            while (!toQuit) {
                if (basicEstimator.isBallUnderOurCtrl()) {
                    info = passFinder.evalPass();
                    if (info == null) {
                        continue;
                    }

                    if (CoordinatedPass.getPassState() == PassState.PENDING) {
                        passer = (Ally) basicEstimator.getBallHolder();
                        receiver = info.getOptimalReceiver();
                        passFinder.fixCandidate(receiver.getID()); // lock receiver
                    }

                    passState = CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, info);
                    System.out.println(passState);
                    switch (passState) {
                        case PASSED -> {
                            if (passer == null) {
                                toQuit = true;
                            } else {
                                receiver.getBall(ball);
                            }
                        }
                        case RECEIVE_SUCCESS -> {
                            passer = receiver;
                            passer.curveTo(info.getOptimalReceivingPos());
                            receiver = info.getOptimalReceiver();
                        }
                        case FAILED -> {
                            toQuit = true;
                            testRtn = false;
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
