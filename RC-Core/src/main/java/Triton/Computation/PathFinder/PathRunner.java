package Triton.Computation.PathFinder;

import java.util.ArrayList;

import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Config.PathfinderConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;

public class PathRunner implements Runnable {
    ArrayList<Vec2D> path;
    double angle;

    private Subscriber<RobotData> robotDataSub;
    private Publisher<Commands> commandsPub;

    public PathRunner(Team team, int ID, ArrayList<Vec2D> path, double angle) {
        this.path = path;
        this.angle = angle;

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);
        commandsPub = new MQPublisher<Commands>("commands", team.name() + ID);

    }

    @Override
    public void run() {
        robotDataSub.subscribe();

        RobotData robotData = robotDataSub.getMsg();
        double startDistToEndPoint = Vec2D.dist(robotData.getPos(), path.get(path.size() - 1));

        double distToOvershootPoint = 0;

        for (int i = 0; i < path.size(); i++) {
            Vec2D node = path.get(i);
            do {
                robotData = robotDataSub.getMsg();
                Vec2D currPos = robotData.getPos();
                double currAngle = robotData.getOrient();

                double distToEndPoint = Vec2D.dist(robotData.getPos(), path.get(path.size() - 1));

                Commands.Builder command = Commands.newBuilder();
                command.setMode(0);
                Vec2D overshoot = node.sub(currPos).norm().mult(PathfinderConfig.OVERSHOOT_DIST);
                Vec2D overshootPoint = node.add(overshoot);
                Vec3D.Builder dest = Vec3D.newBuilder();
                dest.setX(overshootPoint.x);
                dest.setY(overshootPoint.y);

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

                distToOvershootPoint = Vec2D.dist(overshootPoint, currPos);
            } while (distToOvershootPoint > PathfinderConfig.OVERSHOOT_DIST);
        }
    }
}
