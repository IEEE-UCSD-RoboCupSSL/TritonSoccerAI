package Triton.CoreModules.Robot.Ally.AdvancedSkills;

import Triton.Config.GlobalVariblesAndConstants.GvcPathfinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;

import static Triton.Config.OldConfigs.ObjectConfig.DRIBBLER_OFFSET;

public class DynamicIntercept {
    private static final double interpolationRate = 0.5;
    public static final double INTERCEPT_CIRCLE_RADIUS = 80;

    public static void exec(Ally ally, Ball ball) {
        Vec2D ballPos = ball.getPos();
        Vec2D ballVel = ball.getVel();
        if(ballVel.mag() > 2000) {
            ballVel = ballVel.normalized().scale(2000);
        }
        ballVel = ballVel.scale(interpolationRate);
        Vec2D estBallPos = ballPos.add(ballVel);

        if(ballVel.mag() < 500) {
            exec(ally, ball, ballPos, ally.getDir());
        } else {
            exec(ally, ball, estBallPos, ballVel.scale(-1).toPlayerAngle());
        }
    }

    public static void exec(Ally ally, Ball ball, Vec2D estBallPos, double faceDir) {


        Vec2D currPos = ally.getPos();



        if (currPos.sub(estBallPos).mag() < GvcPathfinder.AUTOCAP_DIST_THRESH) {
            ally.autoCap();
        } else {
            Vec2D faceVec = new Vec2D(faceDir).normalized();
            Vec2D offset = faceVec.scale(DRIBBLER_OFFSET);
            Vec2D targetPos = estBallPos.sub(offset);
            ArrayList<Vec2D> circCenters = getCircleCenters(estBallPos, targetPos,
                    faceVec, offset.mag(), INTERCEPT_CIRCLE_RADIUS);

            // System.out.println(circCenters);

            Vec2D interceptPoint = getInterceptPoint(ally.getPos(), targetPos, faceVec, INTERCEPT_CIRCLE_RADIUS, circCenters);

            //System.out.println(interceptPoint);
            ally.curveTo(interceptPoint, estBallPos.sub(ally.getPos()).toPlayerAngle());
        }
    }

    public static void exec(Ally ally, Ball ball, double faceDir) {


        Vec2D currPos = ally.getPos();

        Vec2D estBallPos = ball.getPos();


        if (currPos.sub(estBallPos).mag() < GvcPathfinder.AUTOCAP_DIST_THRESH) {
            ally.autoCap();
        } else {
            Vec2D faceVec = new Vec2D(faceDir).normalized();
            Vec2D offset = faceVec.scale(DRIBBLER_OFFSET);
            Vec2D targetPos = ball.getPos().sub(offset);
            ArrayList<Vec2D> circCenters = getCircleCenters(ball.getPos(), targetPos,
                    faceVec, offset.mag(), INTERCEPT_CIRCLE_RADIUS);

            // System.out.println(circCenters);

            Vec2D interceptPoint = getInterceptPoint(ally.getPos(), targetPos, faceVec, INTERCEPT_CIRCLE_RADIUS, circCenters);

            //System.out.println(interceptPoint);
            ally.curveTo(interceptPoint, ball.getPos().sub(ally.getPos()).toPlayerAngle());
        }
    }

    private static ArrayList<Vec2D> getCircleCenters(Vec2D ballPos, Vec2D targetPos, Vec2D faceVec, double ballTargetDist, double circRad) {
        Vec2D circCentersVec = new Vec2D(faceVec.y, -faceVec.x);
        Vec2D ballTargetMidpoint = ballPos.add(targetPos).scale(0.5);
        double centerMidpointDist = Math.pow(circRad * circRad - 1 / 4 * ballTargetDist * ballTargetDist, 0.5);
        ArrayList<Vec2D> circCenters = new ArrayList<>();
        circCenters.add(ballTargetMidpoint.add(circCentersVec.scale(centerMidpointDist)));
        circCenters.add(ballTargetMidpoint.sub(circCentersVec.scale(centerMidpointDist)));
        return circCenters;
    }

    private static Vec2D getInterceptPoint(Vec2D allyPos, Vec2D targetPos, Vec2D faceVec, double circRad, ArrayList<Vec2D> circCenters) {
        Vec2D targetToCenter = circCenters.get(0).sub(targetPos);
        // double alpha = Vec2D.angleDiff(faceVec, targetToCenter);
        double alpha = 20;

        Vec2D allyToTarget = targetPos.sub(allyPos);
        double angleDiff = PerspectiveConverter.calcAngDiff(allyToTarget.scale(-1.0).toPlayerAngle(),
                faceVec.scale(-1.0).toPlayerAngle());

        //System.out.println(angleDiff);

        Vec2D vel = new Vec2D(0, 0);
        if (Math.abs(angleDiff) < alpha) {
            // vel = targetPos.sub(allyPos).normalized();
            vel = allyToTarget;
            // System.out.println("Baga");
        } else {

            // Determine which circle is closer to the robot
            double dist0 = allyPos.sub(circCenters.get(0)).mag();
            double dist1 = allyPos.sub(circCenters.get(1)).mag();
            double botToCenterDist = Math.min(dist0, dist1);
            Vec2D center = dist0 <= dist1 ? circCenters.get(0) : circCenters.get(1);
            double angDir = -1.0;
            if(dist0 < dist1) angDir = 1.0;
            // System.out.println(center);

            Vec2D allyToCenter = center.sub(allyPos).normalized();
            // Robot outside of both circles
            if (isOutsideCircles(circCenters, circRad, allyPos)) {
                // Find the tangent direction and the point of tangency
                double tangentLength = Math.pow(botToCenterDist * botToCenterDist - circRad * circRad, 0.5);

                if(Math.abs(tangentLength) < 0.01) tangentLength = 0.01;
                double tangentAngle = Math.atan2(circRad, tangentLength);

                Vec2D tangentVec = allyToCenter.rotate(Math.toDegrees(angDir * tangentAngle) + angDir * 30);
                vel = tangentVec.scale(tangentLength);
            } else { // Inside of either circle
                vel = center.sub(allyPos).rotate(angDir * Math.toDegrees(65)).scale(1.5);
                //System.out.println("Inside:" + vel);
            }
        }
        //return allyPos.add(vel.scale(1000));

        return allyPos.add(vel);
    }

    private static boolean isCloserToFaceVec(Vec2D faceVec, Vec2D targetPos, Vec2D allyPos, double alpha) {
        Vec2D allyToTarget = targetPos.sub(allyPos);
        return Math.abs(Vec2D.angleDiff(faceVec, allyToTarget)) <= alpha;
    }

    private static boolean isOutsideCircles(ArrayList<Vec2D> circCenters, double circRad, Vec2D allyPos) {
        return Vec2D.dist(allyPos, circCenters.get(0)) > circRad && Vec2D.dist(allyPos, circCenters.get(1)) > circRad;
    }
}
