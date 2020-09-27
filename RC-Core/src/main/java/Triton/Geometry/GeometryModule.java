package Triton.Geometry;

import Triton.Shape.*;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import Triton.DesignPattern.PubSubSystem.Publisher;
import Triton.DesignPattern.PubSubSystem.Module;
import java.util.*;
import Proto.MessagesRobocupSslGeometry.*;

public class GeometryModule implements Module {
    private Subscriber<SSL_GeometryData> geoSub;
    private Publisher<SSL_GeometryFieldSize> fieldSizePub;
    private Publisher<HashMap<String, Line2D>> fieldLinesPub;

    public GeometryModule() {
        geoSub = new Subscriber<SSL_GeometryData>("vision", "geometry", 1);
        fieldSizePub = new Publisher<SSL_GeometryFieldSize>("geometry", "fieldSize");
        fieldLinesPub = new Publisher<HashMap<String, Line2D>>("geometry", "fieldLines");
    }

    public void run() {
        while (!geoSub.subscribe());

        while (true) {
            SSL_GeometryData geoData = geoSub.pollMsg();
            fieldSizePub.publish(geoData.getField());

            List<SSL_FieldLineSegment> lineList = geoData.getField().getFieldLinesList();
            HashMap<String, Line2D> lineMap = new HashMap<String, Line2D>();
            for (SSL_FieldLineSegment line : lineList) {
                Vec2D p1 = new Vec2D(line.getP1().getX(), line.getP1().getY());
                Vec2D p2 = new Vec2D(line.getP2().getX(), line.getP2().getY());
                lineMap.put(line.getName(), new Line2D(p1, p2));
            }
            fieldLinesPub.publish(lineMap);
        }
    }
}
