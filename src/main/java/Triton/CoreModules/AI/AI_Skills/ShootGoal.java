package Triton.CoreModules.AI.AI_Skills;

import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally;
import Triton.CoreModules.Robot.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.Misc.Math.Matrix.Vec2D;

import static Triton.Config.ObjectConfig.MAX_KICK_VEL;

public class ShootGoal extends Skills {

    Ball ball;
    private final Ally shooter;
    private final RobotList<Foe> foes;
    private final double precisionTolerance = 15; // mm

    // Gridify

    public ShootGoal(Ally shooter, RobotList<Foe> foes, Ball ball) {
        this.shooter = shooter;
        this.foes = foes;
        this.ball = ball;
    }


    public boolean shoot() {
        boolean hasKicked = false;
        // goal width & pos

        //ObjectConfig.EXCESSIVE_DRIBBLING_DIST
        // circle
        // get shoot pos & angle
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
