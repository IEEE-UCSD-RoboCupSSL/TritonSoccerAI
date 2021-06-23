package Triton.CoreModules.AI;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.CoreModules.AI.AI_Strategies.DEPRECATED_BasicPlay;
import Triton.CoreModules.AI.AI_Strategies.Strategies;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.GameStates.*;
import Triton.SoccerObjects;
import Triton.VirtualBot.SimulatorDependent.ErForce.ErForceClientModule;

import static Triton.Misc.Math.Coordinates.PerspectiveConverter.audienceToPlayer;
import static Triton.Util.delay;


public class AI implements Module {
    private static final double KICK_DIST = 100;
    private static final double STOP_DIST = 500;
    private static final double BALL_DIST = 500;
    
    private final RobotList<Ally> fielders;
    private final Ally keeper;
    private final RobotList<Foe> foes;
    private final Ball ball;

    private final Strategies strategyToPlay;
    private final GameCtrlModule gameCtrl;

    private final AttackSupportMapModule atkSupportMap;
    private final PassProbMapModule passProbMap;

    private GameState prevState = new GameState(GameStateName.HALT);

    private Config config;
    private BasicEstimator basicEstimator;

    public AI(Config config, SoccerObjects soccerObjects, GameCtrlModule gameCtrl) {
        this.fielders = soccerObjects.fielders;
        this.keeper = soccerObjects.keeper;
        this.foes = soccerObjects.foes;
        this.ball = soccerObjects.ball;
        if (fielders == null || keeper == null || foes == null || ball == null || gameCtrl == null) {
            throw new NullPointerException();
        }
        this.config = config;
        this.gameCtrl = gameCtrl;
        atkSupportMap = new AttackSupportMapModule(soccerObjects);
        passProbMap = new PassProbMapModule(soccerObjects);
        strategyToPlay = new DEPRECATED_BasicPlay(config, soccerObjects, atkSupportMap, passProbMap);
        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
    }





    @Override
    public void run() {
        try {
            while (true) { // delay added
                GameState currGameState = gameCtrl.getGameState();
                if(currGameState.getName() != prevState.getName()) {
                    System.out.println(">>>>>>" + currGameState + "<<<<<<");
                }
                switch (currGameState.getName()) {
                    case HALT, TIMEOUT -> {
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case STOP -> {
                        long t0 = System.currentTimeMillis();
                        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
                            ErForceClientModule.turnAllDribOff();
                        }
                        while(System.currentTimeMillis() - t0 < 1900
                                && gameCtrl.getGameState().getName() == GameStateName.STOP) {
                            delay(3);
                            Vec2D bpos = ball.getPos();
                            for (Ally fielder : fielders) {
                                Vec2D fpos = fielder.getPos();
                                if (fpos.sub(bpos).mag() < STOP_DIST) {
                                    fielder.slowTo(bpos.sub(fpos).scale(-1000));
                                } else {
                                    Rect2D[] pas = GvcGeometry.getPenaltyRegions(1500);
                                    if(pas[0].isInside(fpos) || pas[1].isInside(fpos)) {
                                        fielder.slowTo(new Vec2D(0, 0));
                                    } else {
                                        fielder.stop();
                                    }
                                }
                            }
                            Vec2D kpos = keeper.getPos();
                            if (kpos.sub(bpos).mag() < STOP_DIST) {
                                keeper.slowTo(bpos.sub(kpos).scale(-1000));
                            } else {
                                keeper.stop();
                            }
                        }
                        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
                            ErForceClientModule.resetTurnAllDribOff();
                        }
                        while(gameCtrl.getGameState().getName() == GameStateName.STOP) {
                            fielders.stopAll();
                            keeper.stop();
                            delay(3);
                        }
                        fielders.stopAll();
                        keeper.stop();
                    }
                    case PREPARE_DIRECT_FREE, PREPARE_INDIRECT_FREE, PREPARE_KICKOFF, PREPARE_PENALTY -> {
                        preparedStart();
                    }
                    case NORMAL_START, FORCE_START -> {
                        handleTooCloseToPenaltiesFoul(fielders);
                        strategyToPlay.play();
                    }
                    case BALL_PLACEMENT -> {
                        BallPlacementGameState ballPlacementGameState = (BallPlacementGameState) currGameState;
                        Team ballPlacementTeam = ballPlacementGameState.getTeam();
//
//                        if (ballPlacementTeam == config.myTeam) {
//                            Vec2D teamTargetPos = PerspectiveConverter.audienceToPlayer(ballPlacementGameState.getTargetPos());
//                            // ballPlacement(teamTargetPos);
//                        } else {
//
//                        }

                        for (Ally fielder : fielders) {
                            fielder.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, false);
                        }
                        keeper.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, false);
                        Formation.getInstance().moveToFormation("ballplacement-defense", fielders, keeper);
                        for (Ally fielder : fielders) {
                            fielder.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, true);
                        }
                        keeper.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, true);

                    }
                }
                prevState = currGameState;
                delay(3);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void preparedStart() throws InterruptedException {
        switch (prevState.getName()) {
            case PREPARE_KICKOFF  -> { // Ad Hoc
                if (prevState.getTeam() == config.myTeam) {
                    for (Ally fielder : fielders) {
                        fielder.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, false);
                    }
                    keeper.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, false);

                    long t0 = System.currentTimeMillis();
                    int cnt = 0;
                    while (cnt < 20 && gameCtrl.getGameState().getName() == GameStateName.PREPARE_KICKOFF) {
                        if(Formation.getInstance().moveToFormation("kickoff-offense", fielders, keeper)) {
                            cnt++;
                        }
                        delay(3);
                    }

                    for (Ally fielder : fielders) {
                        fielder.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, true);
                    }
                    keeper.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, true);

                    while(gameCtrl.getGameState().getName() == GameStateName.PREPARE_KICKOFF) {
                        fielders.stopAll();
                        keeper.stop();
                        delay(3);
                    }
                } else {
                    Formation.getInstance().moveToFormation("kickoff-defense", fielders, keeper);
                }
            }
            case PREPARE_DIRECT_FREE ->{
                AD_HOC_handleStart(GameStateName.PREPARE_DIRECT_FREE);
            }
            case PREPARE_INDIRECT_FREE -> {
                AD_HOC_handleStart(GameStateName.PREPARE_INDIRECT_FREE);
            }

            case PREPARE_PENALTY -> {
                if (prevState.getTeam() == config.myTeam) {

                } else {
//                    Vec2D ballPos = ball.getPos();
//                    Vec2D ourGoalPos = new Vec2D(0, -4500); //Ad Hoc
//
//                    Vec2D aimDir = ourGoalPos.sub(ballPos).normalized();
//                    Mat2D perpenAimDir1 = Mat2D.rotation(90).mult(new Mat2D(aimDir.toEJML()));

                    Formation.getInstance().moveToFormation("penalty-defense", fielders, keeper);

                }
            }
            default -> {}
        }


    }



    private void AD_HOC_handleStart(GameStateName name) {
        handleTooCloseToPenaltiesFoul(fielders);
        Ally starter = basicEstimator.getNearestFielderToBall();
        while(!starter.isHoldingBall() && gameCtrl.getGameState().getName() == name) {
            starter.getBall(ball);
            delay(3);
        }

//        starter.moveAt(new Vec2D(0, 50));
//        starter.spinTo(starter.getDir());
//        delay(300);
//        starter.stop();


    }


    // To-do: add to ini
    public static double penaltySafetyOffset = 1000;

    public static void handleTooCloseToPenaltiesFoul(RobotList<Ally> fielders) {
        // unlock all fielders
        for(Ally bot : fielders) {
            bot.setMotionLocked(false);
        }

        for(Rect2D penalty : GvcGeometry.getPenaltyRegions(penaltySafetyOffset)) {
            for(Ally bot : fielders) {
                if(penalty.isInside(bot.getPos())) {
                    System.out.println("Bot " + bot.getID() + " :Close to Penalty!");
                    pushAllyAwayFromPenalties(bot, penaltySafetyOffset);
                    // lock the motion of the nearly fouled bot in later code of the current outer iteration
                    bot.setMotionLocked(true);
                }
            }
        }
    }

    public static void pushAllyAwayFromPenalties(Ally ally, double safetyOffset) {
        Vec2D allyPos = ally.getPos();
        for(Circle2D pCircle : GvcGeometry.getPenaltyCircles(safetyOffset)) {
            Vec2D outVec = allyPos.sub(pCircle.center).normalized().scale(pCircle.radius);
            ally.curveTo(allyPos.add(outVec));
        }
    }

}


