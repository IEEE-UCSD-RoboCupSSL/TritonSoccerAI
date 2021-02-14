package Triton.PeriphModules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Config.ObjectConfig;
import Triton.CoreModules.Robot.Team;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import org.javatuples.Pair;

import java.util.LinkedList;

/**
 * Stores data about robot object
 */
public class RobotData {

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

    public void update(SSL_DetectionRobot detection, double time) {
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
}