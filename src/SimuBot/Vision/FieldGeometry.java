package SimuBot.Vision;
import SimuBot.DesignPattern.*;


public class FieldGeometry implements Observer {
    public GeometryData geometry;
    String className = "FieldGeometry";
    public FieldGeometry() {}
    public void update(AbstractData data) {
        this.geometry = (GeometryData)data;
    }

    public GeometryData getGeometry() {
        return geometry;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return geometry.toString();
    }
}
