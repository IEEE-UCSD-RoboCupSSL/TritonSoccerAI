package Triton.AI;

import Triton.AI.Estimators.Estimator;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;
import Triton.Objects.Foe;
import Triton.Objects.Robot;

import java.util.ArrayList;

public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final Robot[] allies;
    private final Robot[] foes;
    private final Ball ball;

    private Estimator estimator;

    public AI(Ally[] allies, Foe[] foes, Ball ball) {
        this.allies = allies;
        this.foes = foes;
        this.ball = ball;

        estimator = new Estimator(allies, foes, ball);
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



//    ArrayList<Vec2D> innerPath = new ArrayList<>();
//            innerPath.add(new Vec2D(1000, 1000));
//                    innerPath.add(new Vec2D(1000, -1000));
//                    innerPath.add(new Vec2D(-1000, -1000));
//                    innerPath.add(new Vec2D(-1000, 1000));
//
//                    int innerOffset = 0;
//
//                    ArrayList<Boolean> innerReady = new ArrayList<>();
//        for (int i = 0; i < 4; i++) {
//        innerReady.add(false);
//        }
//
//        ArrayList<Vec2D> outerPath = new ArrayList<>();
//        outerPath.add(new Vec2D(2000, 2000));
//        outerPath.add(new Vec2D(-2000, -2000));
//        outerPath.add(new Vec2D(2000, -2000));
//        outerPath.add(new Vec2D(-2000, 2000));
//
//        int outerOffset = 0;
//
//        ArrayList<Boolean> outerReady = new ArrayList<>();
//        for (int i = 4; i < 6; i++) {
//        outerReady.add(false);
//        }
//
//        while (true) {
//        if (!innerReady.contains(false)) {
//        innerOffset = (innerOffset + 1) % innerPath.size();
//        for (int i = 0; i < 4; i++) {
//        innerReady.set(i, false);
//        }
//        }
//
//        for (int i = 0; i < 4; i++) {
//        Ally ally = allies[i];
//        Vec2D pos = ally.getData().getPos();
//        int index = (i + innerOffset) % innerPath.size();
//        Vec2D node = innerPath.get(index);
//        double dist = Vec2D.dist(node, pos);
//        if (dist <= 200) {
//        innerReady.set(i, true);
//        continue;
//        }
//        ally.sprintTo(node);
//        }
//
//        if (!outerReady.contains(false)) {
//        outerOffset = (outerOffset + 1) % outerPath.size();
//        for (int i = 4; i < 6; i++) {
//        outerReady.set(i - 4, false);
//        }
//        }
//
//        for (int i = 4; i < 6; i++) {
//        Ally ally = allies[i];
//        Vec2D pos = ally.getData().getPos();
//        int index = (i + outerOffset) % outerPath.size();
//        Vec2D node = outerPath.get(index);
//        double dist = Vec2D.dist(node, pos);
//        if (dist <= 200) {
//        outerReady.set(i - 4, true);
//        continue;
//        }
//        ally.sprintTo(node);
//        }
//        }
