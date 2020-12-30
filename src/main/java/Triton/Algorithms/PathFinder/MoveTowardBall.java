package Triton.Algorithms.PathFinder;

import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.*;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Modules.Detection.BallData;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;

import java.util.concurrent.TimeoutException;

public class MoveTowardBall implements Module {

    private Ally ally;
    private Ball ball;

    public MoveTowardBall(Ally ally, Ball ball) {
        this.ally = ally;
        this.ball = ball;
    }

    @Override
    public void run() {
        while (true) {
            Vec2D allyPos = ally.getData().getPos();
            Vec2D ballPos = ball.getData().getPos();
            Vec2D dirBallToRobot = allyPos.sub(ballPos).norm();
            Vec2D dirOffset = dirBallToRobot.mult(ObjectConfig.BALL_RADIUS + ObjectConfig.ROBOT_RADIUS + 50);
            Vec2D target = ballPos.add(dirOffset);
            ally.moveTo(target, 0);
        }
    }
}
