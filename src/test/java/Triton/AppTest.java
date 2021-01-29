package Triton;

import Triton.Config.ObjectConfig;
import Triton.CoreModules.AI.AI;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotFactory;
import Triton.CoreModules.Robot.RobotList;
import Triton.PeriphModules.Detection.DetectionModule;
import Triton.PeriphModules.FieldGeometry.FieldGeometryModule;
import Triton.PeriphModules.Vision.GrSimVisionModule;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static Triton.Config.SimConfig.TOTAL_THREADS;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);

        /* Prepare a Thread Pool*/
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(TOTAL_THREADS, TOTAL_THREADS,
                0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());

        /* Instantiate & Run each independent modules in a separate thread from the thread threadPool */
        Runnable visionModule = new GrSimVisionModule();
        Runnable geoModule = new FieldGeometryModule();
        Runnable detectModule = new DetectionModule();
        threadPool.submit(visionModule);
        threadPool.submit(geoModule);
        threadPool.submit(detectModule);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /* Instantiate & run Ball module*/
        Ball ball = new Ball();
        threadPool.submit(ball);

        RobotList<Ally> allies = RobotFactory.createAllyBots(ObjectConfig.ROBOT_COUNT - 1, threadPool);
        Ally goalKeeper = RobotFactory.createGoalKeeperBot(threadPool);
        RobotList<Foe> foes = RobotFactory.createFoeBotsForTracking(ObjectConfig.ROBOT_COUNT, threadPool);
        if (allies.connectAll() == ObjectConfig.ROBOT_COUNT - 1
                && goalKeeper.connect()) {
            allies.runAll(threadPool);
            threadPool.submit(goalKeeper);
        }
        foes.runAll(threadPool); // submit all to threadPool

        /* Instantiate & Run the main AI module, which is the core of this software */
        Runnable ai = new AI(allies, goalKeeper, foes, ball);
        threadPool.submit(ai);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigorous Test :-)
     */
    public void testApp() {
    }
}
