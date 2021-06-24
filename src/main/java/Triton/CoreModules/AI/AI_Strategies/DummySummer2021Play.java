package Triton.CoreModules.AI.AI_Strategies;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcAI;
import Triton.Config.GlobalVariblesAndConstants.GvcGeneral;
import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.CoreModules.AI.AI_Skills.Swarm;
import Triton.CoreModules.AI.AI_Tactics.AttackPlanSummer2021;
import Triton.CoreModules.AI.AI_Tactics.DefendPlanA;
import Triton.CoreModules.AI.AI_Tactics.FillGapGetBall;
import Triton.CoreModules.AI.Estimators.AttackSupportMapModule;
import Triton.CoreModules.AI.Estimators.BasicEstimator;
import Triton.CoreModules.AI.Estimators.PassProbMapModule;
import Triton.CoreModules.AI.GoalKeeping.GoalKeeping;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.Robot;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.SoccerObjects;

import java.util.ArrayList;

import static Triton.Config.OldConfigs.ObjectConfig.MAX_KICK_VEL;
import static Triton.Misc.Math.Coordinates.PerspectiveConverter.normAng;

public class DummySummer2021Play extends Strategies {

    private States currState = States.START;

    private final RobotList<Ally> fielders;
    private final RobotList<Foe> foes;
    private final Ally keeper;
    private final Ball ball;
    private final BasicEstimator basicEstimator;
    private final GoalKeeping goalKeeping;

    private final AttackSupportMapModule atkSupportMap;
    private final PassProbMapModule passProbMap;

    public DummySummer2021Play(Config config, SoccerObjects soccerObjects, AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap) {
        this(soccerObjects.fielders, soccerObjects.keeper, soccerObjects.foes, soccerObjects.ball,
                atkSupportMap, passProbMap, config);
    }

    public DummySummer2021Play(RobotList<Ally> fielders, Ally keeper,
                               RobotList<Foe> foes, Ball ball,
                               AttackSupportMapModule atkSupportMap, PassProbMapModule passProbMap, Config config) {
        super();
        this.fielders = fielders;
        this.foes = foes;
        this.keeper = keeper;
        this.ball = ball;
        this.atkSupportMap = atkSupportMap;
        this.passProbMap = passProbMap;
        atkSupportMap.run();
        passProbMap.run();

        basicEstimator = new BasicEstimator(fielders, keeper, foes, ball);
        goalKeeping = new GoalKeeping(keeper, ball, basicEstimator);

        // construct tactics
        // attack = new AttackPlanSummer2021(fielders, keeper, foes, ball, atkSupportMap, passProbMap, config);
        getBall = new FillGapGetBall(fielders, keeper, foes, ball, atkSupportMap, config);
        defend = new DefendPlanA(fielders, keeper, foes, ball, 300, config);
    }

    public States getCurrState() {
        return currState;
    }

    public static enum States {
        START,
        DEFEND,
        ATTACK,
        GETBALL,
    }


    @Override
    public void play() {

        switch (currState) {
            case START -> {
                if (basicEstimator.isAllyHavingTheBall()) {
                    currState = States.ATTACK;
                } else {
                    if (basicEstimator.isBallWithinOurReach()) {
                        currState = States.GETBALL;
                    } else {
                        currState = States.DEFEND;
                    }
                }
            }
            case DEFEND -> {
                defend.exec();
                if (basicEstimator.isBallWithinOurReach()) {
                    currState = States.START;
                }
            }
            case GETBALL -> {
                //getBall.exec();
                Ally fielder = basicEstimator.getNearestFielderToBall();
                fielder.dynamicIntercept(ball, GvcGeometry.GOAL_CENTER_FOE.sub(fielder.getPos()).toPlayerAngle());

                if (basicEstimator.isAllyHavingTheBall()) {
                    currState = States.ATTACK;
                }
            }
            case ATTACK -> {
                Ally attacker = null;
                Robot holder = basicEstimator.getBallHolder();
                if (holder instanceof Ally) {
                    attacker = (Ally) holder;

                    if(GvcGeometry.GOAL_CENTER_FOE.sub(attacker.getPos()).mag() > 4000) {
                        Vec2D pos = attacker.getPos();
                        if(pos.x > 1000) {
                            if(attacker.isDirAimed(45)) {
                                attacker.kick(new Vec2D(3, 2));
                            } else {
                                attacker.rotateTo(45);
                            }
                        } else {
                            if(pos.x < -1000) {
                                if(attacker.isDirAimed(-45)) {
                                    attacker.kick(new Vec2D(3, 2));
                                } else {
                                    attacker.rotateTo(-45);
                                }
                            }else {
                                attacker.kick(new Vec2D(2, 3));
                            }
                        }

                    } else {
                        shootingProcedure(attacker);
                    }
                }
                currState = States.START;
            }
        }


        goalKeeping.passiveGuarding();
    }


    private void shootingProcedure(Ally shooter) {
        System.out.println("GOOD LUCK");
        if  (!shooter.isHoldingBall()) {
            return;
        }
        else {
            adhocShooting(shooter);
        }
        RobotList<Ally> restFielders = (RobotList<Ally>) fielders.clone();
        restFielders.remove(shooter);
        //restOfAllyFillGap(restFielders, ball.getPos());
        ArrayList<Vec2D> sidePos = new ArrayList<>();
        sidePos.add(new Vec2D(2800, 500));
        sidePos.add(new Vec2D(2800, 0));
        sidePos.add(new Vec2D(-2800, 500));
        sidePos.add(new Vec2D(-2800, 0));
        new Swarm(restFielders, GvcAI.globalConfig_AdHoc).groupTo(sidePos);
    }


    private void adhocShooting(Ally shooter) {
        Vec2D leftGoalPole = new Vec2D(-500, 4500);
        Vec2D rightGoalPole = new Vec2D(500, 4500);
        Vec2D shooterPos = shooter.getPos();
        double leftAngle = leftGoalPole.sub(shooterPos).toPlayerAngle();
        double rightAngle = rightGoalPole.sub(shooterPos).toPlayerAngle();
        double currAim = shooter.getDir();
        double leftOffset = normAng(currAim - leftAngle);
        double rightOffset = normAng(currAim - rightAngle);
        if (leftOffset < 0 && rightOffset > 0) {
            // aiming inside goal direction

            // check if good to shoot
            Line2D currAimLine = new Line2D(shooterPos.add(new Vec2D(currAim)).scale(100), shooterPos);
            Robot nearAimLineBot = basicEstimator.getNearestBotToLine(currAimLine);
            if (nearAimLineBot == null) return;
            if (nearAimLineBot.getPos().distToLine(currAimLine) < 150) {
                shooter.kick(new Vec2D(MAX_KICK_VEL, 0));
                return;
            }

            // scanning
            Vec2D scanPos = new Vec2D(-500, 4500);
            Vec2D optPos = null;
            double maxScore = 0;
            for (int i = 1; i < 10; i++) {
                Line2D shootingLine = new Line2D(scanPos, shooterPos);
                Robot nearestBot = basicEstimator.getNearestBotToLine(shootingLine);
                if (nearestBot == null) return;
                double score = nearestBot.getPos().distToLine(shootingLine);
                if (score > maxScore) {
                    maxScore = score;
                    optPos = scanPos;
                }
                scanPos = scanPos.add(new Vec2D(i * 100, 0));
            }

            double optAngle = optPos.sub(shooterPos).toPlayerAngle();
            shooter.rotateTo(optAngle);

        } else { // outside goal direction
            if (Math.abs(leftOffset) < Math.abs(rightOffset)) {
                shooter.rotateTo(normAng(leftAngle - 10));

            } else {
                shooter.rotateTo(normAng(rightAngle + 10));
            }
        }


    }
}
