package Triton.CoreModules.AI;

import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.AI.Strategies.Attack.BasicAttack;
import Triton.CoreModules.AI.Strategies.Defense.BasicDefense;
import Triton.CoreModules.AI.Strategies.SeizeOpportunity.ForwardFilling;
import Triton.CoreModules.AI.Strategies.Strategies;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.Misc.Coordinates.Vec2D;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;


public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final RobotList<Ally> allies;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private final Formation formation;
    private final Estimator estimator;
    private final GoalKeeping goalKeeping;
    private final Strategies seizeOpportunity;
    private final Strategies attack;
    private final Strategies defense;


    public AI(RobotList<Ally> allies, Ally keeper, RobotList<Foe> foes, Ball ball) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;

        // To-do: check nullptr for inputs

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
            case START -> startBranch();
            case ACTIVE -> activeBranch();
            case PAUSED -> pausedBranch();
            case DEBUG -> debugBranch();
            default -> defaultBranch();
        }
    }

    private void startBranch() {
        goalKeeping.passiveGuarding();

        Ally shooter = allies.get(0);

        if (shooter.getDribblerStatus()) {
            shooter.sprintToAngle(new Vec2D(1000, -2000), 170);
        } else {
            shooter.getBall();
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
        return GameStates.START;
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


