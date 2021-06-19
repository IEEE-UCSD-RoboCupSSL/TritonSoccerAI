package Triton.CoreModules.Robot.Ally.KeeperSkills;

import Triton.Config.Config;
import Triton.Config.GlobalVariblesAndConstants.GvcPathfinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.ProceduralSkills.Dependency.ProceduralTask;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.Subscriber;
import Triton.PeriphModules.Detection.RobotData;

import java.util.ArrayList;

import static Triton.Config.GlobalVariblesAndConstants.GvcGeometry.*;

public class Keep {

    public static void exec(Config config, Ally ally, Ball ball, Vec2D aimTraj,
                            ArrayList<Subscriber<RobotData>>  blueRobotSubs,
                            ArrayList<Subscriber<RobotData>> yellowRobotSubs) {
        ArrayList<Subscriber<RobotData>> allySubs = (config.myTeam == Team.BLUE) ? blueRobotSubs : yellowRobotSubs;
        ArrayList<Subscriber<RobotData>> foeSubs = (config.myTeam == Team.BLUE) ? yellowRobotSubs : blueRobotSubs;

        // if keeper is holding ball, pass to closest ally
        if (ally.isHoldingBall()) {
            double closestDist2AllyToBall = Double.MAX_VALUE;
            Vec2D passPos = null;
            for (int id = 0; id < config.numAllyRobots; id++) {
                RobotData allyData = allySubs.get(id).getMsg();

                double currDist2AllyToBall = Vec2D.dist2(ally.getPos(), ally.getPos());
                if ((allyData.getID() == allyData.getID() && currDist2AllyToBall < closestDist2AllyToBall) ||
                        passPos == null) {
                    passPos = allyData.getPos();
                    closestDist2AllyToBall = currDist2AllyToBall;
                }
            }
            GoalPassTask goalPassTask = new GoalPassTask(ally, ball, passPos);
            if (!ally.isProcedureCompleted()) {
                ally.executeProceduralTask(goalPassTask);
            }
        }

        // else, check if the ball is in penalty and keeper is closer to the ball than the nearest foe
        Vec2D currPos = ally.getPos();
        Vec2D ballPos = ball.getPos();
        double y = -FIELD_LENGTH / 2 + 150;

        boolean ballInPenalty = ballPos.x > PENALTY_STRETCH_LEFT &&
                ballPos.x < PENALTY_STRETCH_RIGHT &&
                ballPos.y < PENALTY_STRETCH_Y;

        double dist2KeeperToBall = Vec2D.dist2(currPos, ballPos);
        boolean isKeeperCloser = true;
        for (int id = 0; id < config.numAllyRobots; id++) {
            RobotData foeData = foeSubs.get(id).getMsg();
            if (Vec2D.dist2(foeData.getPos(), ballPos) < dist2KeeperToBall) {
                isKeeperCloser = false;
                break;
            }
        }

        // if so, get the ball
        // else, guard the goal
        if (ballInPenalty && isKeeperCloser) {
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

            ally.kick(new Vec2D(2, 2));

            return true;
        }
    }
}
