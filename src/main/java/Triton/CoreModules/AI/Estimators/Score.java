package Triton.CoreModules.AI.Estimators;

import Triton.CoreModules.AI.Estimators.TimeEstimator.BallMovement;
import Triton.CoreModules.AI.Estimators.TimeEstimator.RobotMovement;
import Triton.CoreModules.Robot.RobotSnapshot;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;

import java.util.ArrayList;

public abstract class Score {

    protected static final double PASS_VEL = 2.5;
    protected static final double SHOOT_VEL = 4.0;
    protected static final double ROBOT_PADDING = 200.0;
    protected static final double FRONT_PADDING = 100.0;

    protected Vec2D ballPos;
    protected ArrayList<RobotSnapshot> fielderSnaps, foeSnaps;
    protected double[] passMaxPair; // max distance and time ball travelled with vel
    protected double[] shootMaxPair;

    /**
     * Init score calculator with necessary ball and robot info
     */
    public Score(ProbMapModule finder) {
        this(finder.ballPosWrapper.get(), finder.fielderSnaps, finder.foeSnaps);
    }

    public Score(Vec2D ballPos, ArrayList<RobotSnapshot> fielderSnaps,
                 ArrayList<RobotSnapshot> foeSnaps) {
        this.ballPos = ballPos;
        this.fielderSnaps = fielderSnaps;
        this.foeSnaps = foeSnaps;

        passMaxPair  = BallMovement.calcMaxDist(PASS_VEL);
        shootMaxPair = BallMovement.calcMaxDist(SHOOT_VEL);
    }

    /**
     * Calculate the probability at a specific field position
     * @param pos the position to examine
     * @return the probability
     */
    public abstract double prob(Vec2D pos);


    /**
     * Shortcut for calculating the estimated time from a robot to a destination
     * @param snap robot info
     * @param dest destination location
     * @return the estimated time, assuming zero final speed
     */
    protected double calcETA(RobotSnapshot snap, Vec2D dest) {
        return RobotMovement.calcETA(snap.getDir(), snap.getVel(), dest, snap.getPos());
    }


    /**
     * Shortcut for checking whether an angle is in a range
     * @param angle the angle to examine
     * @param angleRange two angles that specifies a range
     * @return whether the angle is between the two
     */
    protected static boolean angleBetween(double angle, double[] angleRange) {
        double totalDiff = angDiff(angleRange[0], angleRange[1]);
        return angDiff(angle, angleRange[0]) < totalDiff && angDiff(angle, angleRange[1]) < totalDiff;
    }


    /**
     * A shortcut for calculating the angle difference between two angles
     * @param a1 angle1
     * @param a2 angle2
     * @return the absolute angle difference
     */
    protected static double angDiff(double a1, double a2) {
        return Math.abs(PerspectiveConverter.calcAngDiff(a1, a2));
    }


    /**
     * Calculate angles of two rays starting from a position that precisely spans a robot
     * @param robotPos the position of robot
     * @param start the position of the rays' starting point
     * @return angles of two rays
     */
    protected static double[] angleRange(Vec2D robotPos, Vec2D start) {
        Vec2D robotLeftPos = robotPos.add(-ROBOT_PADDING, 0);
        Vec2D robotRightPos = robotPos.add(ROBOT_PADDING, 0);
        Vec2D robotUpPos = robotPos.add(0, ROBOT_PADDING);
        Vec2D robotDownPos = robotPos.add(0, -ROBOT_PADDING);
        double[] angles = new double[] {robotLeftPos.sub(start).toPlayerAngle(),
                robotRightPos.sub(start).toPlayerAngle(), robotUpPos.sub(start).toPlayerAngle(),
                robotDownPos.sub(start).toPlayerAngle()};
        int[][] combination = new int[][] {{0, 1}, {0, 2}, {0, 3}, {1, 2}, {1, 3}, {2, 3}};
        int[] maxIdx = combination[0];
        double maxDiff = 0;
        for (int[] idx : combination) {
            double diff = angDiff(angles[idx[0]], angles[idx[1]]);
            if (diff > maxDiff) {
                maxDiff = diff;
                maxIdx = idx;
            }
        }
        return new double[] {angles[maxIdx[0]], angles[maxIdx[1]]};
    }
}
