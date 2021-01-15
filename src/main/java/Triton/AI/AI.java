package Triton.AI;

import Triton.AI.Estimators.Estimator;
import Triton.AI.GoalKeeping.GoalKeeping;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;
import Triton.Objects.Foe;
import Triton.Objects.Robot;

import java.util.concurrent.ThreadPoolExecutor;

public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final Ally[] allies;
    private final Ally goalKeeper;
    private final Foe[] foes;
    private final Ball ball;

    private final Estimator estimator;
    private final GoalKeeping goalKeeping;

    public AI(Ally[] allies, Ally goalKeeper, Foe[] foes, Ball ball) {
        this.allies = allies;
        this.goalKeeper = goalKeeper;
        this.foes = foes;
        this.ball = ball;

        estimator = new Estimator(allies, goalKeeper, foes, ball);
        goalKeeping = new GoalKeeping(goalKeeper, ball, estimator);
    }

    @Override
    public void run() {

        try {
            while (true) {
                //printBallHolder();
                printBallTraj();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printBallHolder() {
        Robot bot = estimator.getBallHolder();

        if (bot != null) {
            System.out.println(bot.getTeam() + " " + bot.getID());
        } else {
            System.out.println("No bot holding ball");
        }
    }

    private void printBallTraj() {
        Vec2D ballTraj = estimator.getAimTrajectory();

        if (ballTraj != null) {
            System.out.println(ballTraj);
        } else {
            System.out.println("No bot holding ball");
        }
    }
}


