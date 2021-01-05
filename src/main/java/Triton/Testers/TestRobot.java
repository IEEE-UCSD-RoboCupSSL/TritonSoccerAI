package Triton.Testers;

import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;

public class TestRobot implements Module {
    private static final double KICK_DIST = 100;

    private final Ally ally;
    private final Ball ball;

    public TestRobot(Ally ally, Ball ball) {
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public void run() {
        while (true) {
            try {
//                System.out.println("SPIN TO 90");
//                ally.spinTo(90);
//                Thread.sleep(2000);
//
//                System.out.println("SPIN TO -90");
//                ally.spinTo(-90);
//                Thread.sleep(2000);
//
//                System.out.println("ROTATE TO 90");
//                ally.rotateTo(90);
//                Thread.sleep(2000);
//
//                System.out.println("ROTATE TO -90");
//                ally.rotateTo(-90);
//                Thread.sleep(2000);

                System.out.println("PATH TO 0, 2000");
                ally.pathTo(new Vec2D(0, 2000), 90);
                Thread.sleep(6000);

                System.out.println("PATH TO 0, -2000");
                ally.pathTo(new Vec2D(0, -2000), 90);
                Thread.sleep(6000);

                System.out.println("SPRINT TO 0, 2000");
                ally.sprintTo(new Vec2D(0, 2000));
                Thread.sleep(6000);

                System.out.println("SPRINT TO 0, -2000");
                ally.sprintTo(new Vec2D(0, -2000));
                Thread.sleep(6000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void TDRD(double x, double y, double angle) {
        ally.moveTo(new Vec2D(x, y));
        ally.spinTo(angle);
    }

    private void TDRV(double x, double y, double angVel) {
        ally.moveTo(new Vec2D(x, y));
        ally.spinAt(angVel);
    }

    private void TVRD(double velX, double velY, double angle) {
        ally.moveAt(new Vec2D(velX, velY));
        ally.spinTo(angle);
    }

    private void TVRV(double velX, double velY, double angVel) {
        ally.moveAt(new Vec2D(velX, velY));
        ally.spinAt(angVel);
    }

    private void pathToBall() {
        Vec2D allyPos = ally.getData().getPos();
        Vec2D ballPos = ball.getData().getPos();
        Vec2D dirBallToAlly = allyPos.sub(ballPos).norm();
        Vec2D dirOffset = dirBallToAlly.mult(KICK_DIST);
        Vec2D target = ballPos.add(dirOffset);
        Vec2D allyToBall = ballPos.sub(allyPos);
        ally.pathTo(target, allyToBall.toPlayerAngle());
    }
}
