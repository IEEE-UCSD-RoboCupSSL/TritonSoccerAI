package SimuBot;

public class FieldGeometry implements Observer {
    public GeometryType geometry;
    String className = "FieldGeometry";
    FieldGeometry() {}
    public void update(AbstractData data) {
        this.geometry = (GeometryType)data;
    }

    public GeometryType getGeometry() {
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
