package Triton.Command;

import Triton.Config.SimConfig;
import Triton.Detection.Team;
import Triton.Shape.Vec2D;
import Triton.WorldSim.RobotSim;

public class MoveToCommand extends Command {

    private Team   team;
    private int    ID;
    private Vec2D  dest;
    private int    speed; // raw unit per ms

    public MoveToCommand(Team team, int ID, double x, double y, int speed) {
        this.team = team;
        this.ID = ID; 
        this.dest = new Vec2D(x, y);
        this.speed = speed;
    }

    public void execute() {
        RobotSim robot = world.getRobot(team, ID);
        if (!executed) {
            if (Vec2D.dist(robot.getPos(), dest) < SimConfig.DEST_THRESH) {
                robot.setVel(new Vec2D(0, 0));
                executed = true;
                return;
            }

            Vec2D dir = dest.sub(robot.getPos()).norm();
            Vec2D vel = dir.mult(speed);
            robot.setVel(vel);
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

    public int getSpeed() {
        return speed;
    }
}