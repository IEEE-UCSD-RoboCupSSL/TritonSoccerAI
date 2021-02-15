package Triton.CoreModules.Ball;

import Triton.Misc.Math.Matrix.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.Misc.ModulePubSubSystem.Module;
import Triton.PeriphModules.Detection.BallData;

import static Triton.Config.ObjectConfig.POS_PRECISION;

public class Ball implements Module {

    private final FieldSubscriber<BallData> dataSub;

    public Ball() {
        dataSub = new FieldSubscriber<>("detection", "ball");
    }

    public Vec2D getTrajectoryConstraint() {
        return getData().getVel().normalized();
    }

    protected BallData getData() {
        return dataSub.getMsg();
    }

    public Vec2D getVel() {
        return getData().getVel();
    }

    public Vec2D getPos() {
        return getData().getPos();
    }

    public double getTime() {
        return getData().getTime();
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

    public boolean isPosArrived(Vec2D pos) {
        return isPosArrived(pos, POS_PRECISION);
    }

    public boolean isPosArrived(Vec2D pos, double dist) {
        return pos.sub(getPos()).mag() < dist;
    }
}