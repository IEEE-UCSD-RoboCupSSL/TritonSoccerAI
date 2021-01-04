package Triton.Algorithms;

import Triton.Config.ObjectConfig;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;
import Triton.Objects.Foe;

public class AI implements Module {
    private static final double KICK_DIST = 0;

    private final Ally[] allies;
    private final Foe[] foes;
    private final Ball ball;

    public AI(Ally[] allies, Foe[] foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;
    }

    @Override
    public void run() {
        while (true) {
            for (Ally ally : allies) {
                Vec2D allyPos = ally.getData().getPos();
                Vec2D ballPos = ball.getData().getPos();
                Vec2D dirBallToAlly = allyPos.sub(ballPos).norm();
                Vec2D dirOffset = dirBallToAlly.mult(KICK_DIST);
                Vec2D target = ballPos.add(dirOffset);
                Vec2D allyToBall = ballPos.sub(allyPos);
                ally.moveTo(target);
                ally.rotateTo(allyToBall.toPlayerAngle());
                ally.kick(new Vec2D(4, 4));
            }
        }
    }
}
