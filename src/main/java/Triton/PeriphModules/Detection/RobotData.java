package Triton.PeriphModules.Detection;

import Proto.SslVisionDetection;
import Triton.Config.Config;
import Triton.Config.OldConfigs.ObjectConfig;
import Triton.CoreModules.Robot.Side;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import org.javatuples.Pair;

import java.util.Comparator;
import java.util.LinkedList;

import static Triton.Config.GlobalVariblesAndConstants.GvcFilter.smoothing;

/**
 * Stores data about robot object
 */
public class RobotData {
    public static double MAX_POS_LEN = 8000;

    private static class TimePairComparator<T> implements Comparator<Pair<T, Double>> {
        @Override
        public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
            return Double.compare(o1.getValue1(), o2.getValue1());
        }
    }

    private final Team team;
    private final int ID;
    private final LinkedList<Pair<Vec2D, Double>> posList;
    private final LinkedList<Pair<Double, Double>> angleList;
    private Vec2D pos, vel;
    private double angle, angleVel, time;

    public RobotData(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        posList = new LinkedList<Pair<Vec2D, Double>>();
        posList.add(new Pair<Vec2D, Double>(new Vec2D(0, 0), 0.0));

        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);

        angleList = new LinkedList<Pair<Double, Double>>();
        angleList.add(new Pair<Double, Double>(0.0, 0.0));

        angle = 0.0;
        angleVel = 0.0;
        time = 0.0;
    }

    public Team getTeam() {
        return team;
    }

    public int getID() {
        return ID;
    }

    public double getTime() {
        return time;
    }

    public Vec2D getPos() {
        return pos;
    }

    public Vec2D getVel() {
        return vel;
    }

    public double getDir() {
        return angle;
    }

    public double getAngleVel() {
        return angleVel;
    }

//    public boolean verifyPos() {
//        Vec2D pos = getPos();
//        if(pos)
//    }



    public void update(Triton.Legacy.OldGrSimProto.protosrcs.MessagesRobocupSslDetection.SSL_DetectionRobot detection, double time) {
        Vec2D audienceRobotPos = new Vec2D(detection.getX(), detection.getY());
        Vec2D currPos = PerspectiveConverter.audienceToPlayer(audienceRobotPos);
        Pair<Vec2D, Double> posTimePair = new Pair<>(currPos, time);

        double audienceRobotAngle = Math.toDegrees(detection.getOrientation());
        double currAngle = PerspectiveConverter.audienceToPlayer(audienceRobotAngle);
        Pair<Double, Double> angleTimePair = new Pair<>(currAngle, time);

        updatePos(posTimePair);
        updateVel();

        updateAngle(angleTimePair);
        updateAngleVel();
    }


    public Vec2D smoothedValue = new Vec2D(0, 0);



    public void update(SslVisionDetection.SSL_DetectionRobot detection, double time, Config config) {
        Vec2D botPos;
        Vec2D audienceRobotPos;
//        if(config.mySide == Side.GoalToGuardAtLeft) {
            audienceRobotPos = new Vec2D(detection.getX(), detection.getY());
//        } else {
//            audienceRobotPos = new Vec2D(-detection.getX(), -detection.getY());
//        }

        botPos = PerspectiveConverter.audienceToPlayer(audienceRobotPos);

        if(botPos.sub(smoothedValue).mag() > 0.01) {
            smoothedValue = smoothedValue.add((botPos.sub(smoothedValue)).scale(1.00 / smoothing));
            botPos = smoothedValue;
        }

        Pair<Vec2D, Double> posTimePair = new Pair<>(botPos, time);

        double audienceRobotAngle = Math.toDegrees(detection.getOrientation());
        double currAngle = PerspectiveConverter.audienceToPlayer(audienceRobotAngle);
        Pair<Double, Double> angleTimePair = new Pair<>(currAngle, time);

        updatePos(posTimePair);
        updateVel();

        updateAngle(angleTimePair);
        updateAngleVel();
    }

    private void updatePos(Pair<Vec2D, Double> posTimePair) {
        posList.add(posTimePair);
        posList.sort(new TimePairComparator<>());
        if (posList.size() >= ObjectConfig.MAX_POS_LIST_CAPACITY)
            posList.removeFirst();

        pos = posList.getLast().getValue0();
        time = posList.getLast().getValue1();
    }

    private void updateVel() {
        Pair<Vec2D, Double> newestPosTimePair = posList.getLast();
        Vec2D newestPos = newestPosTimePair.getValue0();
        double newestPosTime = newestPosTimePair.getValue1();

        Pair<Vec2D, Double> oldestPosTimePair = posList.getFirst();
        Vec2D oldestPos = oldestPosTimePair.getValue0();
        double oldestPosTime = oldestPosTimePair.getValue1();

        vel = newestPos.sub(oldestPos).scale(1 / (newestPosTime - oldestPosTime));
    }

    private void updateAngle(Pair<Double, Double> angleTimePair) {
        angleList.add(angleTimePair);
        angleList.sort(new TimePairComparator<>());
        if (angleList.size() >= ObjectConfig.MAX_POS_LIST_CAPACITY)
            angleList.removeFirst();

        angle = angleList.getLast().getValue0();
    }

    private void updateAngleVel() {
        Pair<Double, Double> newestAngleTimePair = angleList.getLast();
        double newestAngle = newestAngleTimePair.getValue0();
        double newestAngleTime = newestAngleTimePair.getValue1();

        Pair<Double, Double> oldestAngleTimePair = angleList.getFirst();
        double oldestAngle = oldestAngleTimePair.getValue0();
        double oldestAngleTime = oldestAngleTimePair.getValue1();

        angleVel = (newestAngle - oldestAngle) / (newestAngleTime - oldestAngleTime);
    }
}