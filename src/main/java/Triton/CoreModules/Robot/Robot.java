package Triton.CoreModules.Robot;

import Triton.Misc.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Misc.DesignPattern.PubSubSystem.Module;
import Triton.Misc.Coordinates.Vec2D;
import Triton.PeriphModules.Detection.RobotData;

public abstract class Robot implements Module {
    protected Team team;
    protected int ID;

    private final FieldSubscriber<RobotData> dataSub;

    public Robot(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        dataSub = new FieldSubscriber<>("detection", team.name() + ID);
    }

    protected void subscribe() {
        try {
            dataSub.subscribe(1000);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public RobotData getData() {
        return dataSub.getMsg();
    }

    public Vec2D predPosAtTime(double time) {
        RobotData robotData = getData();
        Vec2D pos = robotData.getPos();
        Vec2D vel = robotData.getVel();
        Vec2D accel = robotData.getAccel();

        return pos.add(vel.mult(time));
//        return pos.add(vel.mult(time)).add(accel.mult(time * time * 0.5));
    }

    public int timeToPoint() {
        return 0;
    }

    @Override
    public void run() {
        try {
            subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Team getTeam() {
        return team;
    }

    public int getID() {
        return ID;
    }
}