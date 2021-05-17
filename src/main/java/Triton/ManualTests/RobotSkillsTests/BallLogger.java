package Triton.ManualTests.RobotSkillsTests;

import Triton.CoreModules.Ball.Ball;
import Triton.Misc.Math.Matrix.Vec2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BallLogger implements Runnable{

        public static final long TIME_INTERVAL = DataCollector.TIME_INTERVAL;
        
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
            double lastPos = Double.MAX_VALUE;
            while (running) {
                try {
                    Vec2D pos = ball.getPos();
                    if (lastPos != pos.y) {
                        logger.info("Ignored", pos.x, pos.y, ball.getTime());
                    }
                    lastPos = pos.y;
                    Thread.sleep(TIME_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

}
