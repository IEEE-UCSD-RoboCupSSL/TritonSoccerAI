package Triton.Modules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionBall;
import Triton.Config.ObjectConfig;
import Triton.Dependencies.PerspectiveConverter;
import Triton.Dependencies.Shape.Vec2D;
import org.javatuples.Pair;

import java.util.ArrayList;

/**
 * Class to store information about the ball
 */
public class BallData {
    private Vec2D pos, vel, accel;
    private final ArrayList<Pair<Vec2D, Double>> posArray, velArray;
    private double time;

    public BallData() {
        posArray = new ArrayList<Pair<Vec2D, Double>>(ObjectConfig.MAX_QUEUE_CAPACITY);
        posArray.add(new Pair<Vec2D, Double>(new Vec2D(0, 0), 0.0));
        velArray = new ArrayList<Pair<Vec2D, Double>>(ObjectConfig.MAX_QUEUE_CAPACITY);
        velArray.add(new Pair<Vec2D, Double>(new Vec2D(0, 0), 0.0));

        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);
        accel = new Vec2D(0, 0);
        time = 0.0;
    }

    /**
     * Updates ArrayList of SortedDetections and calculates the current velocity of the ball
     * @param detection SSL_Detection of the ball
     * @param time time of detection
     */
    public void update(SSL_DetectionBall detection, double time) {
        Vec2D audienceBallPos = new Vec2D(detection.getX(), detection.getY());
        Vec2D currPos = PerspectiveConverter.audienceToPlayer(audienceBallPos);
        Pair<Vec2D, Double> posTimePair = new Pair<>(currPos, time);

        updatePos(posTimePair);
        updateVel();
        updateAccel();
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
}