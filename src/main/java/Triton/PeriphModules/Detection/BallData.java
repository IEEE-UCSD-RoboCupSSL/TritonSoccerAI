package Triton.PeriphModules.Detection;

import Proto.MessagesRobocupSslDetection.SSL_DetectionBall;
import Triton.Config.ObjectConfig;
import Triton.Misc.Math.Coordinates.PerspectiveConverter;
import Triton.Misc.Math.Matrix.Vec2D;
import org.javatuples.Pair;

import java.util.LinkedList;

/**
 * Class to store information about the ball
 */
public class BallData {
    private final LinkedList<Pair<Vec2D, Double>> posList;
    private Vec2D pos, vel;
    private double time;

    public BallData() {
        posList = new LinkedList<Pair<Vec2D, Double>>();
        posList.add(new Pair<Vec2D, Double>(new Vec2D(0, 0), 0.0));

        pos = new Vec2D(0, 0);
        vel = new Vec2D(0, 0);
        time = 0.0;
    }

    /**
     * Updates LinkedList of SortedDetections and calculates the current velocity of the ball
     *
     * @param detection SSL_Detection of the ball
     * @param time      time of detection
     */
    public void update(SSL_DetectionBall detection, double time) {
        Vec2D audienceBallPos = new Vec2D(detection.getX(), detection.getY());
        Vec2D currPos = PerspectiveConverter.audienceToPlayer(audienceBallPos);
        Pair<Vec2D, Double> posTimePair = new Pair<>(currPos, time);

        updatePos(posTimePair);
        updateVel();
    }

    private void updatePos(Pair<Vec2D, Double> posTimePair) {
        posList.add(posTimePair);
        posList.sort(new TimePairComparator<Vec2D>());
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

    public double getTime() {
        return time;
    }

    public Vec2D getPos() {
        return pos;
    }

    public Vec2D getVel() {
        return vel;
    }
}