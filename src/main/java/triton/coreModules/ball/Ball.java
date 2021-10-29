package triton.coreModules.ball;

import triton.misc.math.linearAlgebra.Vec2D;
import triton.misc.modulePubSubSystem.FieldSubscriber;
import triton.periphModules.detection.BallData;

import static triton.config.oldConfigs.ObjectConfig.POS_PRECISION;

public class Ball {

    private final FieldSubscriber<BallData> dataSub;

    public Ball() {
        dataSub = new FieldSubscriber<>("From:DetectionModule", "Ball");
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

    public void subscribe() {
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