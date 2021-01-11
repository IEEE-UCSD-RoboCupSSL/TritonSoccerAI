package Triton.AI;

import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;

public class DataCollector implements Module {
    private static final double POS_THRESHOLD = 100;
    private static final double ANG_THRESHOLD = 2;
    private static final double POS_PRECISION = 0.5;
    private static final double ANG_PRECISION = 0.1;
    private static final double KICK_DIST = 100;
    private static final long TIME_INTERVAL = 20;

    private final Ally ally;
    private final Ball ball;

    public DataCollector(Ally ally, Ball ball) {
        this.ally = ally;
        this.ball = ball;
    }

    public static double angleDiff(double a1, double a2) {
        double diff = Math.abs(a1 - a2);
        return Math.min(diff, Math.abs(diff - 360));
    }

    public void waitUntilStable(Vec2D targetPos, Double targetAngle, boolean verbose) {
        Vec2D pos;
        double angle;
        do {
            try {
                Thread.sleep(TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pos = ally.getData().getPos();
            angle = ally.getData().getAngle();
            if(verbose) {
                System.out.println(pos + "," + angle + "," + ally.getData().getTime());
            }
        } while (targetPos != null && pos.sub(targetPos).mag() > POS_THRESHOLD ||
                 targetAngle != null && angleDiff(targetAngle, angle) > ANG_THRESHOLD);

        Vec2D lastPos;
        double lastAngle;
        do {
            lastPos = pos;
            lastAngle = angle;
            try {
                Thread.sleep(TIME_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pos = ally.getData().getPos();
            angle = ally.getData().getAngle();
            if(verbose) {
                System.out.println(pos + "," + angle + "," + ally.getData().getTime());
            }
        } while (pos.sub(lastPos).mag() > POS_PRECISION ||
                 angleDiff(lastAngle, angle) > ANG_PRECISION);

        if(verbose) {
            System.out.println("stable");
        }
    }

    public static Vec2D targetPos(double dist, double angle) {
        double rad = Math.toRadians(angle);
        return new Vec2D(-dist * Math.sin(rad), dist * Math.cos(rad));
    }

    /* Move the test robot back to the origin */
    public void reset(boolean verbose) {
        Vec2D targetPos = new Vec2D(0, 0);
        double targetAngle = 0;
        ally.pathTo(targetPos, targetAngle);
        waitUntilStable(targetPos, targetAngle, verbose);
    }

    /* Test robot path to with different angles */
    public void task2() {
        reset(false);
        Vec2D targetPos;
        double dist = 3000;

        for (double angle = 0; angle < 360; angle++) {
            targetPos = targetPos(dist, angle);
            ally.pathTo(targetPos, 0.0);
            waitUntilStable(targetPos, 0.0, true);
            reset(false);
        }
    }

    /* Test ball moving */
    /*public void task2() {
        Vec2D allyPos = ally.getData().getPos();
        Vec2D ballPos = ball.getData().getPos();
        Vec2D dirBallToAlly = allyPos.sub(ballPos).norm();
        Vec2D dirOffset = dirBallToAlly.mult(KICK_DIST);
        Vec2D target = ballPos.add(dirOffset);
        Vec2D allyToBall = ballPos.sub(allyPos);
        ally.moveTo(target);
        ally.rotateTo(allyToBall.toPlayerAngle());
        ally.kick(new Vec2D(3, 3));

        Vec2D lastPos = ball.getData().getPos();
        boolean flag = false;

        while(true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if ((ballPos = ball.getData().getPos()).sub(lastPos).mag() > 1) {
                flag = true;
                System.out.println(ballPos);
            } else {
                if (flag) {
                    break;
                }
            }
            lastPos = ballPos;
        }
        ally.moveTo(new Vec2D(-100, 0));
        ally.rotateTo(0);
        waitUntilStable(new Vec2D(-100, 0), 0, false);
    }*/

    @Override
    public void run() {
        task2();
    }
}
