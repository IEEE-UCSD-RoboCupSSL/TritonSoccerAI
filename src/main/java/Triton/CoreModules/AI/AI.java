package Triton.CoreModules.AI;

import Triton.Config.Config;
import Triton.CoreModules.AI.AI_Strategies.BasicPlay;
import Triton.CoreModules.AI.AI_Strategies.Strategies;
import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.AI.AI_Tactics.Tactics;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.GapFinder;
import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.GameStates.*;

import static Triton.Config.OldConfigs.ObjectConfig.DRIBBLER_OFFSET;


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

    private GameState prevState;

    private Config config;

    public AI(Config config, RobotList<Ally> fielders, Ally keeper,
              RobotList<Foe> foes, Ball ball, GameCtrlModule gameCtrl) {
        if (fielders == null || keeper == null || foes == null || ball == null || gameCtrl == null) {
            throw new NullPointerException();
        }
        this.config = config;
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

                // Decision Trees
                if (currGameState != prevState) {
                    runNewState(currGameState);
                    prevState = currGameState;
                }
                runCurrentState(currGameState);

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

    private void runNewState(GameState currGameState) throws InterruptedException {
        switch (currGameState.getName()) {
            case HALT -> {
                System.err.println(">>>NEW: HALT<<<");
            }
            case STOP -> {
                System.err.println(">>>NEW: STOP<<<");
            }
            case NORMAL_START -> {
                System.err.println(">>>NEW: NORMAL_START<<<");
                NormalStartGameState normalStartGameState = (NormalStartGameState) currGameState;
                newNormalStart(normalStartGameState);
            }
            case FORCE_START -> {
                System.err.println(">>>NEW: FORCE_START<<<");
            }
            case PREPARE_KICKOFF -> {
                System.err.println(">>>NEW: PREPARE_KICKOFF<<<");
            }
            case PREPARE_PENALTY -> {
                System.err.println(">>>NEW: PREPARE_PENALTY<<<");
            }
            case PREPARE_DIRECT_FREE -> {
                System.err.println(">>>NEW: PREPARE_DIRECT_FREE<<<");
            }
            case PREPARE_INDIRECT_FREE -> {
                System.err.println(">>>NEW: PREPARE_INDIRECT_FREE<<<");
            }
            case TIMEOUT -> {
                System.err.println(">>>NEW: TIMEOUT<<<");
            }
            case BALL_PLACEMENT -> {
                System.err.println(">>>NEW: BALL_PLACEMENT<<<");
            }
            default -> {
                System.err.println(">>>NEW: UNKNOWN<<<");
            }
        }
    }

    private void runCurrentState(GameState currGameState) throws InterruptedException {
        switch (currGameState.getName()) {
            case HALT -> {
                System.err.println(">>>CURRENT: HALT<<<");
                fielders.stopAll();
                keeper.stop();
            }
            case STOP -> {
                System.err.println(">>>CURRENT: STOP<<<");
                fielders.stopAll();
                keeper.stop();
            }
            case NORMAL_START -> {
                System.err.println(">>>CURRENT: NORMAL_START<<<");
                strategyToPlay.play();
            }
            case FORCE_START -> {
                System.err.println(">>>CURRENT: FORCE_START<<<");
                strategyToPlay.play();
            }
            case PREPARE_KICKOFF -> {
                System.err.println(">>>CURRENT: PREPARE_KICKOFF<<<");
                PrepareKickoffGameState prepareKickoffGameState = (PrepareKickoffGameState) currGameState;
                kickOff(prepareKickoffGameState);
            }
            case PREPARE_PENALTY -> {
                System.err.println(">>>CURRENT: PREPARE_PENALTY<<<");
            }
            case PREPARE_DIRECT_FREE -> {
                System.err.println(">>>CURRENT: PREPARE_DIRECT_FREE<<<");
            }
            case PREPARE_INDIRECT_FREE -> {
                System.err.println(">>>CURRENT: PREPARE_INDIRECT_FREE<<<");
            }
            case TIMEOUT -> {
                System.err.println(">>>CURRENT: TIMEOUT<<<");
                fielders.stopAll();
                keeper.stop();
            }
            case BALL_PLACEMENT -> {
                System.err.println(">>>CURRENT: BALL_PLACEMENT<<<");
                BallPlacementGameState ballPlacementGameState = (BallPlacementGameState) currGameState;
                Team ballPlacementTeam = ballPlacementGameState.getTeam();

                if (ballPlacementTeam == config.myTeam) {
                    Vec2D teamTargetPos = PerspectiveConverter.audienceToPlayer(ballPlacementGameState.getTargetPos());
                    ballPlacement(teamTargetPos);
                }
            }
            default -> {
                tmpPlaceHolder(">>>CURRENT: UNKNOWN<<<");
            }
        }
    }

    private void tmpPlaceHolder(String s) {
    }

    private void newNormalStart(NormalStartGameState normalStartGameState) throws InterruptedException {
        switch (prevState.getName()) {
            case PREPARE_KICKOFF -> {
                if (((PrepareKickoffGameState) prevState).getTeam() == config.myTeam) {
                    System.err.println(">>>SWITCH: START_KICKOFF<<<");
                    PrepareKickoffGameState prepareKickoffGameState = (PrepareKickoffGameState) prevState;
                }
            }
            case PREPARE_PENALTY -> {
                if (((PreparePenaltyGameState) prevState).getTeam() == config.myTeam) {
                    System.err.println(">>>SWITCH: START_PENALTY<<<");
                    PreparePenaltyGameState penaltyGameState = (PreparePenaltyGameState) prevState;
                }
            }
            case PREPARE_DIRECT_FREE -> {
                if (((PrepareDirectFreeGameState) prevState).getTeam() == config.myTeam) {
                    System.err.println(">>>SWITCH: START_DIRECT_FREE<<<");
                    PrepareDirectFreeGameState prepareDirectFreeGameState = (PrepareDirectFreeGameState) prevState;
                    freeKick(prepareDirectFreeGameState);
                }
            }
            case PREPARE_INDIRECT_FREE -> {
                if (((PrepareIndirectFreeGameState) prevState).getTeam() == config.myTeam) {
                    System.err.println(">>>SWITCH: START_INDIRECT_FREE<<<");
                    PrepareIndirectFreeGameState prepareIndirectFreeGameState = (PrepareIndirectFreeGameState) prevState;
                }
            }
            default -> {}
        }
    }

    private void kickOff(PrepareKickoffGameState prepareKickoffGameState) {
        if (prepareKickoffGameState.getTeam() == config.myTeam) {
            Formation.getInstance().moveToFormation("kickoff-offense", fielders, keeper);
        } else {
            Formation.getInstance().moveToFormation("kickoff-defense", fielders, keeper);
        }
    }

    private boolean freeKick(PrepareDirectFreeGameState prepareDirectFreeGameState) throws InterruptedException {
        if (prepareDirectFreeGameState.getTeam() == config.myTeam) {
            Tactics getball = strategyToPlay.getGetBallTactics();
            while (!getball.exec()) {
                Thread.sleep(1);
            }
            fielders.stopAll();

            long t0 = System.currentTimeMillis();
            while (System.currentTimeMillis() - t0 < 10000) {
                getball.exec();
            }
            fielders.stopAll();
        } else {
            DefendPlanA tactic = new DefendPlanA(fielders, keeper, foes, ball, 1000);
            fielders.stopAll();

            long t0 = System.currentTimeMillis();
            while (System.currentTimeMillis() - t0 < 10000) {
                tactic.exec();
            }
            fielders.stopAll();
        }

        return true;
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


