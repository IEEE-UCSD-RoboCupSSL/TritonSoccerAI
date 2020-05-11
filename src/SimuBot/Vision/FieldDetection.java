package SimuBot.Vision;
import SimuBot.DesignPattern.*;


public class FieldDetection implements Observer {
    public DetectionData detection;
    String className = "FieldDetection";
    public FieldDetection() {}

    public void update(AbstractData data) {
        this.detection = (DetectionData)data;
    }

    public DetectionData getDetectionInstance() {
        return detection;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return detection.toString();
    }

}