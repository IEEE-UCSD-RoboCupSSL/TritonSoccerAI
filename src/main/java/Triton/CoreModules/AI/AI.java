package Triton.CoreModules.AI;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.CoreModules.AI.AI_Strategies.DEPRECATED_BasicPlay;
import Triton.CoreModules.AI.AI_Strategies.Strategies;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.AI.Estimators.ProbMapModule;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.CoreModules.Robot.Team;
import Triton.ManualTests.CoreTests.RobotSkillsTests.BallLogger;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.GameControl.GameCtrlModule;
import Triton.PeriphModules.GameControl.GameStates.*;
import Triton.SoccerObjects;
import Triton.VirtualBot.SimulatorDependent.ErForce.ErForceClientModule;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.*;
import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.FIELD_LENGTH;
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
                                    Rect2D[] pas = getBiggerPenalityRegions(1200);
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
                            delay(3);
                        }
                    }
                    case PREPARE_DIRECT_FREE, PREPARE_INDIRECT_FREE, PREPARE_KICKOFF, PREPARE_PENALTY -> {
                        preparedStart();
                    }
                    case NORMAL_START, FORCE_START -> {
                        strategyToPlay.play();
                    }
                    //            case PREPARE_KICKOFF -> {
        //                PrepareKickoffGameState prepareKickoffGameState = (PrepareKickoffGameState) currGameState;
        //                kickOff(prepareKickoffGameState);
        //            }
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
            case PREPARE_KICKOFF, PREPARE_DIRECT_FREE, PREPARE_INDIRECT_FREE  -> { // Ad Hoc
                if (prevState.getTeam() == config.myTeam) {
                    for (Ally fielder : fielders) {
                        fielder.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, false);
                    }
                    keeper.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, false);

                    long t0 = System.currentTimeMillis();
                    while (!Formation.getInstance().moveToFormation("kickoff-offense", fielders, keeper)
                                && gameCtrl.getGameState().getName() == GameStateName.PREPARE_KICKOFF) {
                        delay(3);
                    }

                    for (Ally fielder : fielders) {
                        fielder.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, true);
                    }
                    keeper.getPathFinder().setPointObstacle(ball.getPos(), BALL_DIST, true);


                    AD_HOC_handleStart();
                    while(gameCtrl.getGameState().getName() == GameStateName.PREPARE_KICKOFF) delay(3);
                } else {
                    Formation.getInstance().moveToFormation("kickoff-defense", fielders, keeper);
                }
            }
            case PREPARE_PENALTY -> {
                if (prevState.getTeam() == config.myTeam) {
                    long t0 = System.currentTimeMillis();
                    while (!Formation.getInstance().moveToFormation("kickoff-offense", fielders, keeper)
                            && gameCtrl.getGameState().getName() == GameStateName.PREPARE_KICKOFF) {
                        delay(3);
                    }
                    AD_HOC_handleStart();
                    while(gameCtrl.getGameState().getName() == GameStateName.PREPARE_KICKOFF) delay(3);
                } else {
//                    Vec2D ballPos = ball.getPos();
//                    Vec2D ourGoalPos = new Vec2D(0, -4500); //Ad Hoc
//
//                    Vec2D aimDir = ourGoalPos.sub(ballPos).normalized();
//                    Mat2D perpenAimDir1 = Mat2D.rotation(90).mult(new Mat2D(aimDir.toEJML()));

                    Formation.getInstance().moveToFormation("penalty-defense", fielders, keeper);

                }
            }
//            case PREPARE_DIRECT_FREE -> {
//                if (((PrepareDirectFreeGameState) prevState).getTeam() == config.myTeam) {
//                    AD_HOC_handleStart();
//                } else {
//                    Formation.getInstance().moveToFormation("tester", fielders, keeper);
//                }
//            }
//            case PREPARE_INDIRECT_FREE -> {
//                if (((PrepareIndirectFreeGameState) prevState).getTeam() == config.myTeam) {
//                    AD_HOC_handleStart();
//                } else {
//                    Formation.getInstance().moveToFormation("tester", fielders, keeper);
//                }
//            }
            default -> {}
        }


    }

    private void AD_HOC_handleStart() {
        Ally starter = basicEstimator.getNearestFielderToBall();
        while(!starter.isHoldingBall() && gameCtrl.getGameState().getName() == GameStateName.NORMAL_START) {
            starter.getBall(ball);
            delay(3);
        }

//        starter.moveAt(new Vec2D(0, 50));
//        starter.spinTo(starter.getDir());
//        delay(300);
//        starter.stop();

    }


    private void kickOff(PrepareKickoffGameState prepareKickoffGameState) {
        if (prepareKickoffGameState.getTeam() == config.myTeam) {
            Formation.getInstance().moveToFormation("kickoff-offense", fielders, keeper);
        } else {
            Formation.getInstance().moveToFormation("kickoff-defense", fielders, keeper);
        }
    }

//    private boolean freeKick(PrepareDirectFreeGameState prepareDirectFreeGameState) throws InterruptedException {
//        if (prepareDirectFreeGameState.getTeam() == config.myTeam) {
//            Tactics getball = strategyToPlay.getGetBallTactics();
//            while (!getball.exec()) {
//                Thread.sleep(1);
//            }
//            fielders.stopAll();
//
//            long t0 = System.currentTimeMillis();
//            while (System.currentTimeMillis() - t0 < 10000) {
//                getball.exec();
//            }
//            fielders.stopAll();
//        } else {
////            DefendPlanA tactic = new DefendPlanA(fielders, keeper, foes, ball, 1000, config);
////            fielders.stopAll();
////
////            long t0 = System.currentTimeMillis();
////            while (System.currentTimeMillis() - t0 < 10000) {
////                tactic.exec();
////            }
////            fielders.stopAll();
//
//        }
//
//        return true;
//    }

//    private void ballPlacement(Vec2D targetPos) {
//        BasicEstimator basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
//        Ally ally = basicEstimator.getNearestFielderToBall();
//
//        while(!ball.isPosArrived(targetPos) && gameCtrl.getGameState().getName() == GameStateName.BALL_PLACEMENT) {
//            while (!ally.isHoldingBall()) {
//                ally.getBall(ball);
//                delay(3);
//            }
//            ally.slowTo(targetPos.add(new Vec2D(0, -DRIBBLER_OFFSET)));
//            delay(1);
//        }
//
//        ally.stop();
//        delay(100);
//        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
//            ErForceClientModule.turnAllDribOff();
//        }
//        ally.moveAt(new Vec2D(0, -5));
//        delay(300);
//        if(config.cliConfig.simulator == GvcGeneral.SimulatorName.ErForceSim) {
//            ErForceClientModule.resetTurnAllDribOff();
//        }
//
//    }




    public static Rect2D[] getPenaltyRegions() {
        return ProbMapModule.getPenaltyRegions();
    }

    public static Rect2D[] getBiggerPenalityRegions(double largerByAmount) {
        Vec2D lpA = audienceToPlayer(LEFT_PENALTY_STRETCH.p1);
        Vec2D lpB = audienceToPlayer(LEFT_PENALTY_STRETCH.p2);
        Vec2D rpA = audienceToPlayer(RIGHT_PENALTY_STRETCH.p1);
        Vec2D rpB = audienceToPlayer(RIGHT_PENALTY_STRETCH.p2);


        if (lpA.x > lpB.x) {
            Vec2D tmp = lpA;
            lpA = lpB;
            lpB = tmp;
        }
        if (rpA.x > rpB.x) {
            Vec2D tmp = rpA;
            rpA = rpB;
            rpB = tmp;
        }
        double penaltyWidth = lpA.sub(lpB).mag();
        double penaltyHeight;
        if (lpA.y < 0) {
            penaltyHeight = (new Vec2D(lpA.x, -FIELD_LENGTH / 2)).sub(lpA).mag();
        } else {
            penaltyHeight = (new Vec2D(lpA.x, FIELD_LENGTH / 2)).sub(lpA).mag();
        }

        penaltyHeight += 2 * largerByAmount;
        penaltyWidth += 2 * largerByAmount;

        if (lpA.y < rpA.y) {
            Vec2D lpC = new Vec2D(lpA.x, -FIELD_LENGTH / 2);
            return new Rect2D[]{new Rect2D(lpC, penaltyWidth, penaltyHeight),
                    new Rect2D(rpA, penaltyWidth, penaltyHeight)};
        } else {
            Vec2D rpC = new Vec2D(rpA.x, -FIELD_LENGTH / 2);
            return new Rect2D[]{new Rect2D(rpC, penaltyWidth, penaltyHeight),
                    new Rect2D(lpA, penaltyWidth, penaltyHeight)};
        }
    }


}


