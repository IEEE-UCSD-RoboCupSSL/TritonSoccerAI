package Triton.Geometry;

import Proto.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryData;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Triton.DesignPattern.PubSubSystem.Module;
import Triton.DesignPattern.PubSubSystem.*;
import Triton.Shape.Line2D;
import Triton.Shape.Vec2D;

import java.util.HashMap;
import java.util.List;

public class GeometryModule implements Module {
    private final Subscriber<SSL_GeometryData> geoSub;
    private final Publisher<SSL_GeometryFieldSize> fieldSizePub;
    private final Publisher<HashMap<String, Line2D>> fieldLinesPub;

    public GeometryModule() {
        geoSub = new MQSubscriber<>("vision", "geometry");
        fieldSizePub = new FieldPublisher<>("geometry", "fieldSize", null);
        fieldLinesPub = new FieldPublisher<>("geometry", "fieldLines", null);
    }

    private void subscribe() {
        try {
            geoSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            subscribe();

            while (true) {
                SSL_GeometryData geoData = geoSub.getMsg();
                fieldSizePub.publish(geoData.getField());

                List<SSL_FieldLineSegment> lineList = geoData.getField().getFieldLinesList();
                HashMap<String, Line2D> lineMap = new HashMap<>();
                for (SSL_FieldLineSegment line : lineList) {
                    Vec2D p1 = new Vec2D(line.getP1().getX(), line.getP1().getY());
                    Vec2D p2 = new Vec2D(line.getP2().getX(), line.getP2().getY());
                    lineMap.put(line.getName(), new Line2D(p1, p2));
                }
                fieldLinesPub.publish(lineMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
