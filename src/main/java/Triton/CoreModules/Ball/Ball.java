package Triton.CoreModules.Ball;

import Triton.Misc.Math.LinearAlgebra.Vec2D;
import Triton.Misc.ModulePubSubSystem.FieldSubscriber;
import Triton.PeriphModules.Detection.BallData;

import static Triton.Config.OldConfigs.ObjectConfig.POS_PRECISION;

public class Ball {

    private final FieldSubscriber<BallData> dataSub;

    public Ball() {
        dataSub = new FieldSubscriber<>("From:DetectionModule", "Ball");
    }

    synchronized public Vec2D getTrajectoryConstraint() {
        return getData().getVel().normalized();
    }

    protected BallData getData() {
        return dataSub.getMsg();
    }

    synchronized public Vec2D getVel() {
        return getData().getVel();
    }

    synchronized public Vec2D getPos() {
        return getData().getPos();
    }

    synchronized public double getTime() {
        return getData().getTime();
    }

    synchronized public int timeToPoint() {
        return 0;
    }

    public void subscribe() {
        try {
            dataSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    synchronized public boolean isPosArrived(Vec2D pos) {
        return isPosArrived(pos, POS_PRECISION);
    }

    synchronized public boolean isPosArrived(Vec2D pos, double dist) {
        return pos.sub(getPos()).mag() < dist;
    }
}