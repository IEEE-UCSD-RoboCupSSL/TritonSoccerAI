package Triton.AI;

import Triton.AI.Estimators.Estimator;
import Triton.AI.GoalKeeping.GoalKeeping;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.Objects.Ally;
import Triton.Objects.Ball;
import Triton.Objects.Foe;
import Triton.Objects.Robot;

import java.util.ArrayList;

public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final ArrayList<Ally> allies;
    private final Ally keeper;
    private final ArrayList<Foe> foes;
    private final Ball ball;

    private final Formation formation;
    private final Estimator estimator;
    private final GoalKeeping goalKeeping;


    public AI(ArrayList<Ally> allies, Ally keeper, ArrayList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        formation = Formation.getInstance();
        estimator = new Estimator(allies, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, estimator);
    }

    @Override
    public void run() {
        try {
            while (true) {
                DecisionTree();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DecisionTree() {
        GameStates currGameState = getCurrGameState();

        switch (currGameState) {
            case ACTIVE -> activeBranch();
            case PAUSED -> pausedBranch();
            case DEBUG -> debugBranch();
            default -> defaultBranch();
        }
    }

    private void activeBranch() {
        Robot ballHolder = estimator.getBallHolder();
        if (ballHolder == null) {
        }
        else {
            if (ballHolder instanceof Ally) {
                // play Attack
            }
            else if (ballHolder instanceof Foe ){
                // play defense
            }
        }
    }

    private void pausedBranch() {
    }

    private void debugBranch() {
        formation.defaultFormation(allies);
    }

    private void defaultBranch() {
    }

    private GameStates getCurrGameState() {
        // ...
        return GameStates.DEBUG;
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


