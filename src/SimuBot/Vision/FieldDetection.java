package SimuBot.Vision;
import SimuBot.DesignPattern.*;
import SimuBot.Geometry.Point2D;


public class FieldDetection implements Observer {
    public DetectionData currData;
    String className = "FieldDetection";
    public FieldDetection() {}

    public void update(AbstractData data) {
        this.currData = (DetectionData)data;
        
        // To-do log the coord & packet time
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


}