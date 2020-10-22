package Triton.RemoteStation;

import java.util.ArrayList;

import Proto.RemoteAPI.Commands;
import Proto.RemoteAPI.Vec3D;
import Triton.Config.PathfinderConfig;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Detection.RobotData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;

public class RobotCommandUDPStream extends RobotUDPStream {
 
    private Subscriber<RobotData> robotDataSub;
    private Subscriber<ArrayList<Vec2D>> pathSub;

    public RobotCommandUDPStream(String ip, int port, Team team, int ID) {
        super(ip, port, team, ID);

        String name = (team == Team.YELLOW) ? "yellow robot data" + ID : "blue robot data" + ID;
        robotDataSub = new FieldSubscriber<RobotData>("detection", name);

        pathSub = new MQSubscriber<ArrayList<Vec2D>>("path commands", team.name() + ID, 1);
    }

    private void sendCommand(Commands command) {
        byte[] bytes = command.toByteArray();
        send(bytes);
    }

    public void run() {
        robotDataSub.subscribe();
        pathSub.subscribe();

        while (true) {
            ArrayList<Vec2D> path = pathSub.getMsg();
            
            double distToOvershootPoint = 0;
            for (Vec2D point : path) {
                do {
                    RobotData robotData = robotDataSub.getMsg();
                    Vec2D currPos = robotData.getPos();

                    Commands.Builder command = Commands.newBuilder();
                    command.setMode(0);
                    Vec2D overshoot =  point.sub(currPos).norm().mult(PathfinderConfig.OVERSHOOT_DIST);
                    Vec2D overshootPoint = point.add(overshoot);
                    Vec3D.Builder dest = Vec3D.newBuilder();
                    dest.setX(overshootPoint.x);
                    dest.setY(overshootPoint.y);
                    dest.setZ(0);
                    command.setMotionSetPoint(dest);
                    sendCommand(command.build());

                    distToOvershootPoint = Vec2D.dist(overshootPoint, currPos);
                } while (distToOvershootPoint > PathfinderConfig.OVERSHOOT_DIST);
            }
        }
    }
}
