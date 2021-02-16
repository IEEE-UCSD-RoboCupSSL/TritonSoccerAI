package Triton.CoreModules.AI;

import Triton.CoreModules.AI.AI_Strategies.BasicPlay;
import Triton.CoreModules.AI.AI_Strategies.Strategies;
import Triton.CoreModules.AI.AI_Tactics.Tactics;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.GameStates.BallPlacementGameState;
import Triton.PeriphModules.GameControl.GameStates.GameState;

import static Triton.Config.ObjectConfig.DRIBBLER_OFFSET;
import static Triton.Config.ObjectConfig.MY_TEAM;


public class AI implements Module {
    private static final double KICK_DIST = 100;

    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private final Strategies strategyToPlay;
    private final GameCtrlModule gameCtrl;

    private final GapFinder gapFinder;
    private final PassFinder passFinder;

    public AI(RobotList<Ally> fielders, Ally keeper,
              RobotList<Foe> foes, Ball ball, GameCtrlModule gameCtrl) {
        if (fielders == null || keeper == null || foes == null || ball == null || gameCtrl == null) {
            throw new NullPointerException();
        }

        this.fielders = fielders;
        this.keeper = keeper;
        this.foes = foes;
        this.ball = ball;
        this.gameCtrl = gameCtrl;

        gapFinder = new GapFinder(fielders, foes, ball);
        passFinder = new PassFinder(fielders, foes, ball);

        strategyToPlay = new BasicPlay(fielders, keeper, foes, ball,
                                gapFinder, passFinder);
    }

    @Override
    public void run() {
        try {
            while (true) { // delay added
                GameState currGameState = gameCtrl.getGameState();

                // Decision Tree
                switch (currGameState.getName()) {
                    case RUNNING -> {
                        tmpPlaceHolder(">>>RUNNING<<<");
                        strategyToPlay.play();
                    }
                    case FREE_KICK -> {
                        tmpPlaceHolder(">>>FREE_KICK<<<");
                        freeKick();
                    }
                    case KICKOFF -> {
                        tmpPlaceHolder(">>>KICKOFF<<<");
                        kickOff();
                    }
                    case PENALTY -> {
                        tmpPlaceHolder(">>>PENALTY<<<");
                    }
                    case TIMEOUT -> {
                        tmpPlaceHolder(">>>TIMEOUT<<<");
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case HALT -> {
                        tmpPlaceHolder(">>>HALT<<<");
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case STOP -> {
                        tmpPlaceHolder(">>>STOP<<<");
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case BALL_PLACEMENT -> {
                        tmpPlaceHolder(">>>BALL_PLACEMENT<<<");
                        BallPlacementGameState ballPlacementGameState = (BallPlacementGameState) currGameState;
                        Team ballPlacementTeam = ballPlacementGameState.getTeam();
                        if (ballPlacementTeam == MY_TEAM) {
                            Vec2D teamTargetPos = PerspectiveConverter.audienceToPlayer(ballPlacementGameState.getTargetPos());
                            ballPlacement(teamTargetPos);
                        }
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

    private void tmpPlaceHolder(String s) {
    }

    private void kickOff() {
        // To-do: kicking vs defending side
        Formation.getInstance().moveToFormation("kickoff-defense", fielders, keeper);
    }

    private void freeKick() throws InterruptedException {
        Tactics getball = strategyToPlay.getGetBallTactics();
        while(!getball.exec()) {
            Thread.sleep(1);
        }

        fielders.stopAll();
        long t0 = System.currentTimeMillis();
        while(System.currentTimeMillis() - t0 < 1000) {
            getball.exec();
        }
        fielders.stopAll();
    }

    private void ballPlacement(Vec2D targetPos) throws InterruptedException {

        BasicEstimator basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);

        Ally ally = basicEstimator.getNearestFielderToBall();

        while(!ball.isPosArrived(targetPos)) {
            if (ally.isHoldingBall()) {
                ally.curveTo(targetPos.add(new Vec2D(0, -DRIBBLER_OFFSET)), 0);
            } else {
                ally.getBall(ball);
            }
            Thread.sleep(1);
        }

        ally.stop();
        Thread.sleep(500);
        ally.kick(new Vec2D(0.000, 0.01));

    }

}


