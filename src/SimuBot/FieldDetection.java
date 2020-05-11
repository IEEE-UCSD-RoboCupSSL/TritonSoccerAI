package SimuBot;


public class FieldDetection implements Observer {
    public DetectionType detection;
    String className = "FieldDetection";
    FieldDetection() {}

    public void update(AbstractData data) {
        this.detection = (DetectionType)data;
    }

    public DetectionType getDetection() {
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