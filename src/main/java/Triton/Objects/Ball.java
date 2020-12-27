package Triton.Objects;

import Triton.Dependencies.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Modules.Detection.BallData;
import Triton.Modules.Detection.RobotData;

public class Ball implements Module {

    private final FieldSubscriber<BallData> dataSub;

    public Ball() {
        dataSub = new FieldSubscriber<>("detection", "ball");
    }

    protected void subscribe() {
        try {
            dataSub.subscribe();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public BallData getData() {
        return dataSub.getMsg();
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