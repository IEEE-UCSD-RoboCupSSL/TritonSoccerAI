package Triton.CoreModules.Robot;

import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.Detection.RobotData;

public abstract class Robot implements Module {
    private final FieldSubscriber<RobotData> dataSub;
    protected Team team;
    protected int ID;

    public Robot(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        dataSub = new FieldSubscriber<>("From:DetectionModule", team.name() + ID);
    }

    synchronized public Vec2D getPos() {
        return getData().getPos();
    }

    protected RobotData getData() {
        return dataSub.getMsg();
    }

    synchronized public Vec2D getVel() {
        return getData().getVel();
    }

    synchronized public double getDir() {
        return getData().getDir();
    }

    synchronized public int timeToPoint() {
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

    protected void subscribe() {
        try {
            dataSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public Team getTeam() {
        return team;
    }

    synchronized public int getID() {
        return ID;
    }
}