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

    protected void subscribe() {
        try {
            dataSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BallData getData() {
        return dataSub.getMsg();
    }

    public Vec2D getTrajectoryConstraint() {
        return getData().getVel().norm();
    }

    public Vec2D predPosAtTime(double time) {
        BallData ballData = getData();
        Vec2D pos = ballData.getPos();
        Vec2D vel = ballData.getVel();
        Vec2D accel = ballData.getAccel();

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
}