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
        try {
            while (true) {
                ally.receiveBall(new Vec2D(2000, 2000));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
