package Triton.CoreModules.Robot.Ally.KeeperSkills;

import Triton.Config.GlobalVariblesAndConstants.GvcPathfinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.ProceduralTask;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.*;

public class Keep {

    public static void exec(Ally ally, Ball ball, Vec2D aimTraj) {
        if (ally.isHoldingBall()) {
            GoalPassTask goalPassTask = new GoalPassTask(ally, ball, new Vec2D(0, 0));
            if (!ally.isProcedureCompleted()) {
                ally.executeProceduralTask(goalPassTask);
            }
        }

        Vec2D currPos = ally.getPos();
        Vec2D ballPos = ball.getPos();
        double y = -FIELD_LENGTH / 2 + 150;

        if (currPos.sub(ballPos).mag() < GvcPathfinder.AUTOCAP_DIST_THRESH) {
            ally.getBall(ball);
        } else {
            double x;
            if (Math.abs(aimTraj.y) <= 0.0001 || Math.abs(aimTraj.x) <= 0.0001) {
                x = ballPos.x;
            } else {
                double m = aimTraj.y / aimTraj.x;
                double b = ballPos.y - (ballPos.x * m);
                x = (y - b) / m;
            }

            x = Math.max(x, GOAL_LEFT);
            x = Math.min(x, GOAL_RIGHT);
            Vec2D targetPos = new Vec2D(x, y);
            ally.curveTo(targetPos);
        }
    }

    private static class GoalPassTask extends ProceduralTask {
        Ally ally;
        Ball ball;
        Vec2D passPos;

        public GoalPassTask(Ally ally, Ball ball, Vec2D passPos) {
            this.ally = ally;
            this.ball = ball;
            this.passPos = passPos;
        }

        @Override
        public Boolean call() throws Exception {
            Vec2D currPos = ally.getPos();
            Vec2D passVec = passPos.sub(currPos).normalized();
            double passAng = passVec.toPlayerAngle();

            while (!ally.isDirAimed(passAng))
                ally.rotateTo(passAng);

            ally.kick(new Vec2D(10, 10));

            return true;
        }
    }
}
