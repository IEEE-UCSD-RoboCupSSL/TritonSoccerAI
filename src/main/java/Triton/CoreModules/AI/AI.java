package Triton.CoreModules.AI;

import Triton.CoreModules.AI.AI_Strategies.BasicPlay;
import Triton.CoreModules.AI.Estimators.Estimator;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.AI.AI_Strategies.Strategies;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameCtrlModule;


public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final RobotList<Ally> allies;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;


    private final Strategies strategyToPlay;
    private final GameCtrlModule gameCtrl;


    public AI(RobotList<Ally> allies, Ally keeper,
              RobotList<Foe> foes, Ball ball, GameCtrlModule gameCtrl) {
        this.allies = allies;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
        this.gameCtrl = gameCtrl;

        // To-do: check nullptr for inputs


        // future upgrade: use SpringBoot IOC to apply dependency injection here
        strategyToPlay = new BasicPlay(allies, keeper, foes, ball);
    }
    
    private void tmpPlaceHolder(String s) {}

    @Override
    public void run() {
        try {

            System.out.println("Right now, make run won't run anything meaningful yet, use make test instead and practice TDD (Test-Driven Development) ");

            while (true) { // delay added
                GameStates currGameState = gameCtrl.getGameState();

                // Decision Tree
                switch (currGameState) {
                    case RUNNING -> {
                        tmpPlaceHolder(">>>RUNNING<<<");
                        strategyToPlay.play();
                    }
                    case FREE_KICK -> {
                        tmpPlaceHolder(">>>FREE_KICK<<<");
                    }
                    case KICKOFF -> {
                        tmpPlaceHolder(">>>KICKOFF<<<");
                    }
                    case PENALTY -> {
                        tmpPlaceHolder(">>>PENALTY<<<");
                    }
                    case TIMEOUT -> {
                        tmpPlaceHolder(">>>TIMEOUT<<<");
                    }
                    case HALT -> {
                        tmpPlaceHolder(">>>HALT<<<");
                    }
                    case STOP -> {
                        tmpPlaceHolder(">>>STOP<<<");
                    }
                    case BALL_PLACEMENT -> {
                        tmpPlaceHolder(">>>BALL_PLACEMENT<<<");
                    }
                    default -> {
                        tmpPlaceHolder(">>>UNKNOWN<<<");
                    }
                }

                try { // avoid starving other threads
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    private void startBranch() {
//        goalKeeping.passiveGuarding();
//
//        Ally shooter = allies.get(0);
//
//        if (shooter.getDribblerStatus()) {
//            shooter.sprintToAngle(new Vec2D(1000, -2000), 170);
//        } else {
//            shooter.getBall();
//        }
//    }


//    private void printBallHolder() {
//        Robot bot = estimator.getBallHolder();
//
//        if (bot != null) {
//            tmpPlaceHolder(bot.getTeam() + " " + bot.getID());
//        } else {
//            tmpPlaceHolder("No bot holding ball");
//        }
//    }
//
//    private void printBallTraj() {
//        Vec2D ballTraj = estimator.getAimTrajectory();
//
//        if (ballTraj != null) {
//            tmpPlaceHolder(ballTraj);
//        } else {
//            tmpPlaceHolder("No bot holding ball");
//        }
//    }
}


