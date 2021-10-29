package triton.coreModules.robot;

import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.misc.modulePubSubSystem.Module;
import triton.periphModules.detection.RobotData;

public abstract class Robot implements Module {
    private final FieldSubscriber<RobotData> dataSub;
    protected Team team;
    protected int ID;

    public Robot(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        dataSub = new FieldSubscriber<>("From:DetectionModule", team.name() + ID);
    }

    public Vec2D getPos() {
        return getData().getPos();
    }

    protected RobotData getData() {
        return dataSub.getMsg();
    }

    public Vec2D getVel() {
        return getData().getVel();
    }

    public double getDir() {
        return getData().getDir();
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

    protected void subscribe() {
        try {
            dataSub.subscribe(1000);
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