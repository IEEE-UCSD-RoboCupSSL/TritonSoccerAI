package Triton.CoreModules.Ball;

import Triton.Misc.Coordinates.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.Detection.BallData;

public class Ball implements Module {

    private final FieldSubscriber<BallData> dataSub;

    public Ball() {
        dataSub = new FieldSubscriber<>("detection", "ball");
    }

    public Vec2D getTrajectoryConstraint() {
        return getData().getVel().norm();
    }

    public BallData getData() {
        return dataSub.getMsg();
    }

    public Vec2D getPos() {
        return getData().getPos();
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
}