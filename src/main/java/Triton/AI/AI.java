package Triton.AI;

import Triton.AI.Estimators.Estimator;
import Triton.AI.GoalKeeping.GoalKeeping;
import Triton.AI.Strategies.Attack.BasicAttack;
import Triton.AI.Strategies.Defense.BasicDefense;
import Triton.AI.Strategies.SeizeOpportunity.ForwardFilling;
import Triton.AI.Strategies.Strategies;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Vec2D;
import Triton.MovingObjectModules.Ball.Ball;
import Triton.MovingObjectModules.Robot.Ally;
import Triton.MovingObjectModules.Robot.Foe;
import Triton.MovingObjectModules.Robot.Robot;

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
    private final Strategies seizeOpportunity;
    private final Strategies attack;
    private final Strategies defense;


    public AI(ArrayList<Ally> allies, Ally keeper, ArrayList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        formation = Formation.getInstance();

        // future upgrade: use SpringBoot IOC to apply dependency injection here
        estimator = new Estimator(allies, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, estimator);
        seizeOpportunity = new ForwardFilling(allies, foes, ball);
        attack = new BasicAttack(allies, foes, ball);
        defense = new BasicDefense(allies, keeper, foes, ball);
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
            // Eval & SeizeOpportunity
            if(estimator.hasHoldBallChance()) {
                // delegate nearest bot to get ball & push remainder bots forward in attack formation
                seizeOpportunity.play();
            }
            else {
                // rally an defensive formation
                defense.play();
            }
        }
        else {
            if (ballHolder instanceof Ally) {
                // play Attack
                attack.play();
            }
            else if (ballHolder instanceof Foe){
                // play defense
                defense.play();
            }
        }
    }

    private void pausedBranch() {
    }

    private void debugBranch() {
        formation.defaultFormation(allies);
        goalKeeping.passiveGuarding();
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


