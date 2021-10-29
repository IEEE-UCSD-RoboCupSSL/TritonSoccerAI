package triton.manualTests.coreTests.aiSkillsTests;

import triton.config.Config;
import triton.coreModules.ai.skills.DEPRECATED_CoordinatedPass;
import triton.coreModules.ai.skills.PassState;
import triton.coreModules.ai.estimators.BasicEstimator;
import triton.coreModules.ai.estimators.PassProbMapModule;
import triton.coreModules.ai.estimators.PassInfo;
import triton.coreModules.ball.Ball;
import triton.coreModules.robot.ally.Ally;
import triton.coreModules.robot.foe.Foe;
import triton.coreModules.robot.RobotList;
import triton.manualTests.coreTests.robotSkillsTests.RobotSkillsTest;

import java.util.Scanner;

public class DEPRECATED_CPassTest extends RobotSkillsTest {
    Scanner scanner;
    Ally __passer;
    RobotList<Ally> fielders;
    Ball ball;

    BasicEstimator basicEstimator;
    PassProbMapModule passProbMap;
    PassInfo info;

    public DEPRECATED_CPassTest(RobotList<Ally> fielders, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.ball = ball;
        this.fielders = fielders;

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        passProbMap = new PassProbMapModule(fielders, foes, ball);
        passProbMap.run();
    }

    @Override
    public boolean test(Config config) {

        boolean testRtn = true;
        try {
            while(true) {
                /* preparation */
                if (basicEstimator.getBallHolder() == null) {
                    __passer = fielders.get(0);
                    while (!__passer.isHoldingBall()) {
                        __passer.getBall(ball);
                    }
                } else {
                    __passer = (Ally) basicEstimator.getBallHolder();
                }

                /* Begin test */
                PassState passState;
                Ally passer = null;
                Ally receiver = null;
                boolean toQuit = false;

                while (!toQuit) {
                    Thread.sleep(1);
                    if (basicEstimator.isBallUnderOurCtrl()) {
                        info = passProbMap.evalPass();
                        if (info == null) {
                            fielders.stopAll();
                            continue;
                        }

                        if (DEPRECATED_CoordinatedPass.getPassState() == PassState.PENDING) {
                            passer = (Ally) basicEstimator.getBallHolder();
                            receiver = info.getOptimalReceiver();
                            // System.out.println(receiver);
                            // passProbMap.fixCandidate(receiver.getID()); // lock receiver
                        }

                        passState = DEPRECATED_CoordinatedPass.basicPass(passer, receiver, ball, basicEstimator, info);
                        System.out.println(passState);
                        switch (passState) {
                            case PASSED -> {
//                            if (passer == null) {
//                                toQuit = true;
//                            } else {
//                                receiver.getBall(ball);
//                            }
                                passer.stop();
                            }
                            case RECEIVE_SUCCESS -> {
//                            passer = receiver;
//                            passer.curveTo(info.getOptimalReceivingPos());
//                            receiver = info.getOptimalReceiver();
                                receiver.stop();
                                testRtn = true;
                                toQuit = true;
                            }
                            case FAILED -> {
                                fielders.stopAll();
                                toQuit = true;
                                testRtn = false;
                            }
                        }

                    } else {
                        System.out.println("###############Ball Out of Our Control");
                        toQuit = true;
                    }
                }

                fielders.stopAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return testRtn;
    }
}
