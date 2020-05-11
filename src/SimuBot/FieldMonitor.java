package SimuBot;

public class FieldMonitor implements Observer {
    Subject visionConn;
    public DetectionType detection;
    FieldConfiguration config;
    FieldMonitor(Subject visionConn, FieldConfiguration config) {
        this.visionConn = visionConn;
        this.config = config;
    }

    public void update(AbstractData data) {
        this.detection = (DetectionType)data;
    }

    public DetectionType getDetection() {
        return detection;
    }

    @Override
    public String toString() {
        return detection.toString();
    }

}