package Triton.Computation.PathFinder;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Config.PathfinderConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.BallData;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.Shape.Line2D;
import Triton.Shape.Vec2D;

public class PathRunner implements Runnable {
    ArrayList<Vec2D> path;
    double angle;

    private Subscriber<RobotData> robotDataSub;
    private Subscriber<BallData> ballSub;
    private Publisher<Commands> commandsPub;
    private Publisher<String> tcpCommandPub;

    volatile boolean shutdown;

    public PathRunner(ArrayList<Vec2D> path, double angle, Subscriber<RobotData> robotDataSub,
            Subscriber<BallData> ballSub, Publisher<Commands> commandsPub, Publisher<String> tcpCommandPub) {
        this.path = path;
        this.angle = angle;
        this.robotDataSub = robotDataSub;
        this.ballSub = ballSub;
        this.commandsPub = commandsPub;
        this.tcpCommandPub = tcpCommandPub;
    }

    @Override
    public void run() {
        RobotData robotData = robotDataSub.getMsg();
        BallData ballData = ballSub.getMsg();
        double startDistToEndPoint = Vec2D.dist(robotData.getPos(), path.get(path.size() - 1));

        // Whole loop can be ended inside PathRelayer
        for (int i = 1; i < path.size(); i++) {
            if (shutdown)
                return;

            Vec2D node = path.get(i);
            Vec2D overshoot = node.sub(path.get(i - 1)).norm().mult(PathfinderConfig.OVERSHOOT_DIST);
            Vec2D overshootPoint = node.add(overshoot);
            Vec3D.Builder dest = Vec3D.newBuilder();
            dest.setX(-overshootPoint.y);
            dest.setY(overshootPoint.x);

            boolean currSide;
            boolean targetSide;
            double distToNode;
            do {
                if (shutdown) {
                    return;
                }

                Commands.Builder command = Commands.newBuilder();

                robotData = robotDataSub.getMsg();
                Vec2D currPos = robotData.getPos();
                Vec2D ballPos = ballData.getPos();

                if (Vec2D.dist(currPos, ballPos) < PathfinderConfig.BALL_CATCH_DIST) {
                    /*
                    command.setEnableBallAutoCapture(true);
                    tcpCommandPub.publish("request dribbler status"); 
                    */
                    return;
                }

                double currAngle = robotData.getOrient();
                double distToEndPoint = Vec2D.dist(robotData.getPos(), path.get(path.size() - 1));

                command.setMode(0);
                command.setIsWorldFrame(true);

                // z = [(b - a) i] / k + a
                // z is angle at node
                // i is current dist to endpoint
                // k is start dist to endpoint
                // a is start angle
                // b is end angle
                // produces a constant shift in angle per node between angle a and angle b
                // at i = 0, z = a
                // at i = k, z = b
                dest.setZ((angle - currAngle) * distToEndPoint / startDistToEndPoint + currAngle);
                command.setMotionSetPoint(dest);
                commandsPub.publish(command.build());

                if (overshoot.x == 0) {
                    currSide = currPos.y < node.y;
                    targetSide = overshoot.y < node.y;
                } else if (overshoot.y == 0) {
                    currSide = currPos.x < node.x;
                    targetSide = overshoot.x < node.x;
                } else {
                    // y = (-1 / m)(x - x1) + y1
                    // equation is a line perpedicular to slope m that contains point (x1, y1)
                    // m = target slope
                    // x1 = target pos x
                    // y1 = target pos y
                    double m = overshoot.y / overshoot.x;
                    double x1 = node.x;
                    double y1 = node.y;

                    currSide = (currPos.y < (-1 / m) * (currPos.x - x1) + y1);
                    targetSide = (overshoot.y < (-1 / m) * (overshoot.x - x1) + y1);
                }
                
                distToNode = Vec2D.dist(robotData.getPos(), node);

                // continue when current pos and overshoot point on same side of divider line
            } while (currSide != targetSide && distToNode > PathfinderConfig.OVERSHOOT_DIST);
        }
    }

    public void shutdown() {
        shutdown = true;
    }
}
