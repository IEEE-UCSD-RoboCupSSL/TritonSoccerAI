package Triton.CoreModules.AI.AI_Skills;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.ArrayList;

import static Triton.Config.ObjectConfig.EXCESSIVE_DRIBBLING_DIST;
import static Triton.Config.ObjectConfig.MAX_KICK_VEL;

public class ShootGoal extends Skills {

    private final Ally shooter;
    private final RobotList<Foe> foes;
    private final double precisionTolerance = 15; // mm
    Ball ball;
    private final double worldSizeX;
    private final double worldWizeY;
    private final Vec2D goalLeft;
    private final Vec2D goalRight;

    // Gridify

    public ShootGoal(Ally shooter, RobotList<Foe> foes, Ball ball, double worldSizeX, double worldSizeY, Vec2D goalLeft, Vec2D goalRight) {
        this.shooter = shooter;
        this.foes = foes;
        this.ball = ball;

        this.worldSizeX = worldSizeX;
        this.worldWizeY = worldSizeY;

        this.goalLeft = goalLeft;
        this.goalRight = goalRight;
    }

    public ArrayList<Vec2D> findOptimalShootPos(Vec2D ballCapPos) {
        Rect2D field = new Rect2D(new Vec2D(-worldSizeX / 2, -worldWizeY / 2), worldSizeX, worldWizeY);
        double shooterStepSize = 150;
        ArrayList<Vec2D> shootPosList = new ArrayList<>();
        for (double x = ballCapPos.x - EXCESSIVE_DRIBBLING_DIST; x < ballCapPos.x + EXCESSIVE_DRIBBLING_DIST; x += shooterStepSize) {
            for (double y = ballCapPos.y - EXCESSIVE_DRIBBLING_DIST; y < ballCapPos.y + EXCESSIVE_DRIBBLING_DIST; y += shooterStepSize) {
                Vec2D potentialShootPos = new Vec2D(x, y);
                if (field.isInside(potentialShootPos)) {
                    shootPosList.add(potentialShootPos);
                }
            }
        }

        ArrayList<Vec2D> foePosList = new ArrayList<>();
        for (Foe foe : foes) {
            foePosList.add(foe.getPos());
        }

        Vec2D shootPos = null;
        Vec2D target = goalLeft;
        double maxScore = Double.MIN_VALUE;
        double goalStepSize = 150;
        for (Vec2D potentialShootPos : shootPosList) {
            for (double x = goalLeft.x; x < goalRight.x; x += goalStepSize) {
                Vec2D potentialTarget = new Vec2D(x, goalLeft.y);
                Line2D lineToTarget = new Line2D(potentialShootPos, potentialTarget);

                double lineScore = Double.MAX_VALUE;
                for (Vec2D foePos : foePosList) {
                    double newLineScore = foePos.distToLine(lineToTarget);
                    if (newLineScore < lineScore) {
                        lineScore = newLineScore;
                    }
                }

                if (lineScore > maxScore) {
                    shootPos = potentialShootPos;
                    target = potentialTarget;
                    maxScore = lineScore;
                }
            }
        }

        ArrayList<Vec2D> rtn = new ArrayList<>();
        rtn.add(shootPos);
        rtn.add(target);
        return rtn;
    }

    public boolean shoot(Vec2D shootPos, Vec2D target) {
        boolean hasKicked = false;
        double shootAngle = target.sub(shootPos).toPlayerAngle();

//        if (shooter.isPosArrived(shootPos)) {
//            if (shooter.isDirAimed(shootAngle)) {
//                shooter.kick(new Vec2D(MAX_KICK_VEL, 0));
//                hasKicked = true;
//            } else {
//                shooter.dribRotate(ball, shootAngle, 10);
//            }
//        } else {
//            shooter.curveTo(shootPos); // don't rotate yet
//        }

        if (shooter.isPosArrived(shootPos, 200) && shooter.isDirAimed(shootAngle, 20)) {
            shooter.kick(new Vec2D(MAX_KICK_VEL, 0));
            hasKicked = true;
        } else {
            shooter.curveTo(shootPos, shootAngle);
        }

        return hasKicked;
    }

    public boolean trickFoeShoot(RobotList<Foe> foes) {
        boolean hasKicked = false;
        //ObjectConfig.EXCESSIVE_DRIBBLING_DIST

        // To-do (future)
        Vec2D shootPos = new Vec2D(0, 0); // To-do
        double shootAngle = 0; // To-do


        if (shooter.isPosArrived(shootPos)) {
            if (shooter.isDirAimed(shootAngle)) {
                shooter.kick(new Vec2D(MAX_KICK_VEL, 0));
                hasKicked = true;
            } else {
                shooter.dribRotate(ball, shootAngle, 10);
            }
        } else {
            shooter.curveTo(shootPos); // don't rotate yet
        }

        return hasKicked;
    }


}
