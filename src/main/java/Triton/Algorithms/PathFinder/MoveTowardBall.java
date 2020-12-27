package Triton.Algorithms.PathFinder;

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
            ally.moveTo(ball.getData().getPos(), 0);
        }
    }
}
