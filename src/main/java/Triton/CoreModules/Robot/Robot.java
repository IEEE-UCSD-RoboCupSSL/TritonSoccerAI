package Triton.CoreModules.Robot;

import Triton.Config.GlobalVariblesAndConstants.GvcGeometry;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.Detection.RobotData;
import lombok.Getter;
import lombok.Setter;

public abstract class Robot implements Module {
    private final FieldSubscriber<RobotData> dataSub;
    protected Team team;
    protected int ID;

    @Getter
    @Setter
    protected boolean isFoulOut = false;

    public Robot(Team team, int ID) {
        this.team = team;
        this.ID = ID;

        dataSub = new FieldSubscriber<>("From:DetectionModule", team.name() + ID);
    }

    public Vec2D getPos() {
        if (isFoulOut) return new Vec2D(-GvcGeometry.FIELD_WIDTH / 2.0, -GvcGeometry.FIELD_LENGTH / 2.0);
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