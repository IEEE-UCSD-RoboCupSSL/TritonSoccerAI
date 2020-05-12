package SimuBot.Vision;
import SimuBot.DesignPattern.*;
import SimuBot.Geometry.*;


public class FieldDetection implements Observer {
    public DetectionData currData;
    public String className = "FieldDetection";
    
    private double prevCaptureTime = 0.000;
    private double vCalcSamplePeriod = 0.5; // unit: seconds  

    

    public FieldDetection() {}

    public void update(AbstractData data) {
        this.currData = (DetectionData)data;
        
        // To-do: log the coord & packet time


        double deltaT = getCaptureTime() - prevCaptureTime;
        if(deltaT > vCalcSamplePeriod) {
            // To-do: calculate Velocity vector for each robot (\vec{v})

            // To-do: calcular angular velocity vector (\vec{\omega})

        }
        prevCaptureTime = getCaptureTime();
    }

    public DetectionData getCurrDetectedData() {
        return currData;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return currData.toString();
    }

    public Point2D getRobotLoc(boolean isBlue, int robotID) {
        if(robotID < 0 || robotID > DetectionData.NUM_ROBOTS) {
            throw new IndexOutOfBoundsException();
        }
        Point2D location;
        if(isBlue) {
            location = new Point2D(currData.blueRobots[robotID].getX(),
                                   currData.blueRobots[robotID].getY());
        }
        else {
            location = new Point2D(currData.yellowRobots[robotID].getX(),
                                   currData.yellowRobots[robotID].getY());
        }
        return location;
    }

    public Point2D getBallLoc() {
        return new Point2D(currData.ball.getX(), currData.ball.getY());
    }

    public double getCaptureTime() {
        return currData.t_capture;
    }

    public double getSentTime() {
        return currData.t_sent;
    }

    public void setVCalcSamplePeriod(double period) {
        vCalcSamplePeriod = period;
    }
    public void setVCalcSampleFrequency(double freq_hz) {
        vCalcSamplePeriod = 1.000000 / freq_hz;
    }

    public Vec2D getLinearVelocity() {
        // To-do
        return new Vec2D(0,0);
    }
    public Vec2D getV() {
        return getLinearVelocity();
    }

    public double getAngularVelocity() {
        // To-do
        return 0.0;
    }

    public double getW() {
        return getAngularVelocity();
    }
}