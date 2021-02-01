package Triton.PeriphModules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Config.ObjectConfig;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Coordinates.PerspectiveConverter;
import Triton.Misc.Coordinates.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;

/**
 * Stores data about robot object
 */
public class RobotData {

    public static final int MAX_SIZE = 10;
    private final Team team;
    private final int ID;
    private final ArrayList<Pair<Vec2D, Double>> posArray, velArray;
    private final ArrayList<Pair<Double, Double>> angleArray, angleVelArray;
    private Vec2D pos, vel, accel;
    private double angle, angleVel, angleAccel, time;

    public RobotData(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        posArray = new ArrayList<Pair<Vec2D, Double>>(ObjectConfig.MAX_QUEUE_CAPACITY);
        posArray.add(new Pair<Vec2D, Double>(new Vec2D(0, 0), 0.0));
        velArray = new ArrayList<Pair<Vec2D, Double>>(ObjectConfig.MAX_QUEUE_CAPACITY);
        velArray.add(new Pair<Vec2D, Double>(new Vec2D(0, 0), 0.0));

        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);
        accel = new Vec2D(0, 0);

        angleArray = new ArrayList<Pair<Double, Double>>(ObjectConfig.MAX_QUEUE_CAPACITY);
        angleArray.add(new Pair<Double, Double>(0.0, 0.0));
        angleVelArray = new ArrayList<Pair<Double, Double>>(ObjectConfig.MAX_QUEUE_CAPACITY);
        angleVelArray.add(new Pair<Double, Double>(0.0, 0.0));

        angle = 0.0;
        angleVel = 0.0;
        angleAccel = 0.0;
        time = 0.0;
    }

    public void update(SSL_DetectionRobot detection, double time) {
        Vec2D audienceRobotPos = new Vec2D(detection.getX(), detection.getY());
        Vec2D currPos = PerspectiveConverter.audienceToPlayer(audienceRobotPos);
        Pair<Vec2D, Double> posTimePair = new Pair<>(currPos, time);

        double audienceRobotAngle = Math.toDegrees(detection.getOrientation());
        double currAngle = PerspectiveConverter.audienceToPlayer(audienceRobotAngle);
        Pair<Double, Double> angleTimePair = new Pair<>(currAngle, time);

        updatePos(posTimePair);
        updateVel();
        updateAccel();

        updateAngle(angleTimePair);
        updateAngleVel();
        updateAngleAccel();
    }

    private void updatePos(Pair<Vec2D, Double> posTimePair) {
        posArray.add(posTimePair);
        posArray.sort(new TimePairComparator<Vec2D>());
        if (posArray.size() >= ObjectConfig.MAX_QUEUE_CAPACITY)
            posArray.remove(0);

        pos = posArray.get(posArray.size() - 1).getValue0();
        time = posArray.get(posArray.size() - 1).getValue1();
    }

    private void updateVel() {
        Pair<Vec2D, Double> newestPosTimePair = posArray.get(posArray.size() - 1);
        Vec2D newestPos = newestPosTimePair.getValue0();
        double newestPosTime = newestPosTimePair.getValue1();

        Pair<Vec2D, Double> oldestPosTimePair = posArray.get(0);
        Vec2D oldestPos = oldestPosTimePair.getValue0();
        double oldestPosTime = oldestPosTimePair.getValue1();

        Vec2D newestVel = newestPos.sub(oldestPos).mult(1 / (newestPosTime - oldestPosTime));
        Pair<Vec2D, Double> newestVelTimePair = new Pair<Vec2D, Double>(newestVel, newestPosTime);
        velArray.add(newestVelTimePair);
        velArray.sort(new TimePairComparator<Vec2D>());

        if (velArray.size() >= ObjectConfig.MAX_QUEUE_CAPACITY)
            velArray.remove(0);

        vel = velArray.get(velArray.size() - 1).getValue0();
    }

    private void updateAccel() {
        Pair<Vec2D, Double> newestVelTimePair = velArray.get(velArray.size() - 1);
        Vec2D newestVel = newestVelTimePair.getValue0();
        double newestVelTime = newestVelTimePair.getValue1();

        Pair<Vec2D, Double> oldestVelTimePair = velArray.get(0);
        Vec2D oldestVel = oldestVelTimePair.getValue0();
        double oldestVelTime = oldestVelTimePair.getValue1();

        accel = newestVel.sub(oldestVel).mult(1 / (newestVelTime - oldestVelTime));
    }

    private void updateAngle(Pair<Double, Double> angleTimePair) {
        angleArray.add(angleTimePair);
        angleArray.sort(new TimePairComparator<Double>());
        if (angleArray.size() >= ObjectConfig.MAX_QUEUE_CAPACITY)
            angleArray.remove(0);

        angle = angleArray.get(angleArray.size() - 1).getValue0();
    }

    private void updateAngleVel() {
        Pair<Double, Double> newestAngleTimePair = angleArray.get(angleArray.size() - 1);
        double newestAngle = newestAngleTimePair.getValue0();
        double newestAngleTime = newestAngleTimePair.getValue1();

        Pair<Double, Double> oldestAngleTimePair = angleArray.get(0);
        double oldestAngle = oldestAngleTimePair.getValue0();
        double oldestAngleTime = oldestAngleTimePair.getValue1();

        double newestAngleVel = (newestAngle - oldestAngle) / (newestAngleTime - oldestAngleTime);
        Pair<Double, Double> newestAngleVelTimePair = new Pair<Double, Double>(newestAngleVel, newestAngleTime);
        angleVelArray.add(newestAngleVelTimePair);
        angleVelArray.sort(new TimePairComparator<Double>());

        if (angleVelArray.size() >= ObjectConfig.MAX_QUEUE_CAPACITY)
            angleVelArray.remove(0);

        angleVel = angleVelArray.get(angleVelArray.size() - 1).getValue0();
    }

    private void updateAngleAccel() {
        Pair<Double, Double> newestAngleVelTimePair = angleVelArray.get(angleVelArray.size() - 1);
        double newestAngleVel = newestAngleVelTimePair.getValue0();
        double newestAngleVelTime = newestAngleVelTimePair.getValue1();

        Pair<Double, Double> oldestAngleVelTimePair = angleVelArray.get(0);
        double oldestAngleVel = oldestAngleVelTimePair.getValue0();
        double oldestAngleVelTime = oldestAngleVelTimePair.getValue1();

        angleAccel = (newestAngleVel - oldestAngleVel) / (newestAngleVelTime - oldestAngleVelTime);
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

    public Vec2D getAccel() {
        return accel;
    }

    public double getDir() {
        return angle;
    }

    public double getAngleVel() {
        return angleVel;
    }

    public double getAngAccel() {
        return angleAccel;
    }
}