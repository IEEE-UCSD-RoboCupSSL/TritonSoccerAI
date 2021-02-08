package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Scanner;

public class DataCollector extends RobotSkillsTest {

    private static class BallLogger implements Runnable {

        private final Logger logger = LogManager.getLogger(BallLogger.class);
        private final Ball ball;
        private volatile boolean running = true;

        public BallLogger(Ball ball) {
            this.ball = ball;
        }

        public void terminate() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Vec2D pos = ball.getPos();
                    logger.info("Ignored", pos.x, pos.y, ball.getTime());
                    Thread.sleep(TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /* Automated test for data collection */
    Scanner scanner;
    RobotList<Ally> fielders;
    Ally keeper;
    Ball ball;
    BallLogger logger;

    public static final double POS_THRESHOLD = 50;
    public static final double POS_PRECISION = 1;
    public static final double ANG_PRECISION = 0.1;
    public static final long TIME_INTERVAL = 13;
    public static final String LOG_DIR = "src/main/resources/log/";
    public static final String LOG_CACHE = "src/main/resources/application.tmp";

    public DataCollector(Scanner scanner, RobotList<Ally> fielders, Ally keeper, Ball ball) {
        this.scanner = scanner;
        this.fielders = fielders;
        this.keeper = keeper;
        this.ball = ball;
    }

    @Override
    public boolean test() {
        new FormationTest("out", fielders, keeper).test();
        Ally ally = fielders.get(0);

        for (double kickSpeed = 1; kickSpeed < 4; kickSpeed += 0.01) {
            /* Robot 0 dribble ball */

            while (!ally.isHoldingBall()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ally.getBall(ball);
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* Go to the origin */
            rotateTo(ally, ball, 0);
            moveTo(ally, new Vec2D(0, 0), 0);
            rotateTo(ally, ball, 0);

            /* Initialize a ball position logger before kick */
            logger = new BallLogger(ball);
            Thread thread = new Thread(logger);
            thread.start();

            /* Kick, wait until ball stops */
            ally.kick(new Vec2D(kickSpeed, 0));
            Vec2D ballPos = ball.getPos();

            while (ball.getPos().sub(ballPos).mag() < POS_THRESHOLD);
            while (ball.getPos().sub(ballPos).mag() > POS_PRECISION) {
                ballPos = ball.getPos();
                try {
                    Thread.sleep(TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            /* Close the logger */
            logger.terminate();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* Save the log */
            File directory = new File(LOG_DIR);
            if (!directory.exists()){
                directory.mkdir();
            }
            try {
                File cache = new File(LOG_CACHE);
                File toSave = new File(LOG_DIR + String.format("%.2f", kickSpeed) + ".csv");
                FileUtils.copyFile(cache, toSave);
                FileUtils.write(cache, "", Charset.defaultCharset()); // clean up cache
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /* Move robot to a position, with high precision */
    public static void moveTo(Ally ally, Vec2D targetPos, double targetAng) {
        while (!ally.isPosArrived(targetPos) || !ally.isDirAimed(targetAng)) {
            ally.strafeTo(targetPos, targetAng);
        }
        double angle;
        Vec2D pos;
        do {
            angle = ally.getDir();
            pos = ally.getPos();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ally.strafeTo(targetPos, targetAng);
        } while (Math.abs(PerspectiveConverter.calcAngDiff(ally.getDir(), angle)) > ANG_PRECISION ||
            ally.getPos().sub(pos).mag() > POS_PRECISION);
    }

    /* Rotate dribbling robot to an angle, with high precision */
    public static void rotateTo(Ally ally, Ball ball, double targetAng) {
        while (!ally.isDirAimed(targetAng)) {
            ally.dribRotate(ball, targetAng);
        }
        double angle;
        do {
            angle = ally.getDir();
            try {
                Thread.sleep(TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ally.dribRotate(ball, targetAng);
        }
        while (Math.abs(PerspectiveConverter.calcAngDiff(ally.getDir(), angle)) > ANG_PRECISION);
    }
}
