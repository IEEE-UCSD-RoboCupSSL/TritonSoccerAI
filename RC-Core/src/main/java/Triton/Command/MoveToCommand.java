package Triton.Command;

import Proto.MessagesRobocupSslDetection.SSL_DetectionRobot;
import Triton.Detection.DetectionData;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;
import Triton.Config.SimConfig;

public class MoveToCommand extends Command {

    private Team   team;
    private int    ID;
    private Vec2D  dest;
    private double speed; // raw unit per ms

    public MoveToCommand(Team team, int ID, double x, double y, double speed) {
        this.team = team;
        this.ID = ID; 
        this.dest = new Vec2D(x, y);
        this.speed = speed;
    }

    public void execute() {
        if (!executed) {
            DetectionData detect = DetectionData.get();
            double time = System.currentTimeMillis() / 1000.0 - detect.getDeltaT();

            Vec2D start = detect.getRobotPos(team, ID);
            Vec2D dist = dest.sub(start);
            Vec2D next = start.add(dist.norm().mult(speed * SimConfig.EXEC_INTERVAL));

            if (speed * 5 >= dist.mag()) {
                next = dest;
                executed = true;
            }

            SSL_DetectionRobot.Builder rb = SSL_DetectionRobot.newBuilder();
            rb.setX((float) next.x);
            rb.setY((float) next.y);
            rb.setPixelX((float) next.x);
            rb.setPixelY((float) next.y);
            rb.setConfidence(0);
            rb.setRobotId(ID);

            detect.updateTime(time);
            detect.updateRobot(team, ID, rb.build(), time);
        }
    }

    public Team getTeam() {
        return team;
    }

    public int getID() {
        return ID;
    }

    public Vec2D getDest() {
        return dest;
    }

    public double getSpeed() {
        return speed;
    }
}