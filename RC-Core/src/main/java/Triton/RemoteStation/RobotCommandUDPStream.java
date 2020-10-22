package Triton.RemoteStation;

import java.util.ArrayList;

import org.javatuples.Pair;

import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Config.PathfinderConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;

public class RobotCommandUDPStream extends RobotUDPStream {

    private Subscriber<RobotData> robotDataSub;
    private Subscriber<Pair<ArrayList<Vec2D>, Double>> pathSub;

    public RobotCommandUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);

        pathSub = new MQSubscriber<Pair<ArrayList<Vec2D>, Double>>("path commands", team.name() + ID, 1);
    }

    private void sendCommand(Commands command) {
        byte[] bytes = command.toByteArray();
        send(bytes);
    }

    public void run() {
        robotDataSub.subscribe();
        pathSub.subscribe();

        while (true) {
            Pair<ArrayList<Vec2D>, Double> pairWithDir = pathSub.getMsg();
            ArrayList<Vec2D> path = pairWithDir.getValue0();
            double angle = pairWithDir.getValue1();

            double distToOvershootPoint = 0;

            for (int i = 0; i < path.size(); i++) {
                Vec2D node = path.get(i);
                do {
                    RobotData robotData = robotDataSub.getMsg();
                    Vec2D currPos = robotData.getPos();
                    double currAngle = robotData.getOrient();

                    Commands.Builder command = Commands.newBuilder();
                    command.setMode(0);
                    Vec2D overshoot = node.sub(currPos).norm().mult(PathfinderConfig.OVERSHOOT_DIST);
                    Vec2D overshootPoint = node.add(overshoot);
                    Vec3D.Builder dest = Vec3D.newBuilder();
                    dest.setX(overshootPoint.x);
                    dest.setY(overshootPoint.y);

                    // z = [(b - a) i] / k + a
                    // z is angle at node
                    // i is current index of node
                    // k is total number of nodes
                    // a is start angle
                    // b is end angle
                    // produces a constant shift in angle per node between angle a and angle b
                    // at i = 0, z = a
                    // at i = k, z = b
                    // NOTE: SHOULD ANGLE SHIFT BE BASED ON NODE DIST INSTEAD OF NODE INDEX?
                    dest.setZ(((angle - currAngle)  * i) / (path.size() - 1) + currAngle);
                    command.setMotionSetPoint(dest);
                    sendCommand(command.build());

                    distToOvershootPoint = Vec2D.dist(overshootPoint, currPos);
                } while (distToOvershootPoint > PathfinderConfig.OVERSHOOT_DIST);
            }
        }
    }
}
