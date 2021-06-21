package Triton.CoreModules.AI.AI_Skills;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.*;
import static Triton.Config.OldConfigs.ObjectConfig.EXCESSIVE_DRIBBLING_DIST;
import static Triton.Config.OldConfigs.ObjectConfig.MAX_KICK_VEL;

public class ShootGoal extends Skills {

    private final Ally shooter;
    private final RobotList<Foe> foes;
    private final double precisionTolerance = 15; // mm
    Ball ball;

    // Gridify

    public ShootGoal(Ally shooter, RobotList<Foe> foes, Ball ball) {
        this.shooter = shooter;
        this.foes = foes;
        this.ball = ball;
    }

    public ArrayList<Vec2D> findOptimalShootPos(Vec2D ballCapPos) {
        Rect2D field = new Rect2D(FIELD_BOTTOM_LEFT, FIELD_WIDTH, FIELD_LENGTH);
        double shooterStepSize = 400;
        ArrayList<Vec2D> shootPosList = new ArrayList<>();
        for (double x = ballCapPos.x - EXCESSIVE_DRIBBLING_DIST / 2; x < ballCapPos.x + EXCESSIVE_DRIBBLING_DIST / 2; x += shooterStepSize) {
            for (double y = ballCapPos.y - EXCESSIVE_DRIBBLING_DIST / 2; y < ballCapPos.y + EXCESSIVE_DRIBBLING_DIST / 2; y += shooterStepSize) {
                Vec2D potentialShootPos = new Vec2D(x, y);
                if (field.isInside(potentialShootPos)) {
                    shootPosList.add(potentialShootPos);
                }
            }
        }

        Vec2D shootPos = null;
        Vec2D target = null;
        double maxScore = Double.MIN_VALUE;
        double goalStepSize = 200;
        for (Vec2D potentialShootPos : shootPosList) {
            for (double x = GOAL_LEFT; x < GOAL_RIGHT; x += goalStepSize) {
                Vec2D potentialTarget = new Vec2D(x, FIELD_LENGTH / 2);
                Line2D lineToTarget = new Line2D(potentialShootPos, potentialTarget);

                double lineScore = Double.MAX_VALUE;
                for (Foe foe : foes) {
                    Vec2D foePos = foe.getPos();
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

        System.out.println("Target" + shootPos + " : " + shootAngle);
        System.out.println("Shooter" + shooter.getPos() + " : " + shooter.getDir());

        shooter.curveTo(shootPos, shootAngle);
        if (shooter.isPosArrived(shootPos, 100) && shooter.isDirAimed(shootAngle, 5)) {
            shooter.kick(new Vec2D(MAX_KICK_VEL, 0));
            hasKicked = true;
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
