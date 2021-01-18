package Triton.AI.MannualTesters.SkillTests;

import Triton.Dependencies.DesignPattern.PubSubSystem.Module;

import Triton.MovingObjectModules.Ball.Ball;
import Triton.MovingObjectModules.Robot.Ally;

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
        try {
            while (true) {
//                BallData ballData = ball.getData();
//                Vec2D ballPos = ballData.getPos();
//                Vec2D ballVel = ballData.getVel();
//                Vec2D allyPos = ally.getData().getPos();

                ally.receiveBall(ball.predPosAtTime(0.25));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
