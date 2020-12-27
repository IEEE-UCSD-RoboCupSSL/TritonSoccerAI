package Triton.Modules.Geometry;

import Proto.MessagesRobocupSslGeometry.SSL_FieldLineSegment;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryData;
import Proto.MessagesRobocupSslGeometry.SSL_GeometryFieldSize;
import Triton.Dependencies.DesignPattern.PubSubSystem.*;
import Triton.Dependencies.DesignPattern.PubSubSystem.Module;
import Triton.Dependencies.Shape.Line2D;
import Triton.Dependencies.Shape.Vec2D;

import java.util.HashMap;
import java.util.List;

/**
 * Module to process SSL_GeometryData sent from VisionModule
 */
public class GeometryModule implements Module {
    private final Subscriber<SSL_GeometryData> geoSub;
    private final Publisher<SSL_GeometryFieldSize> fieldSizePub;
    private final Publisher<HashMap<String, Line2D>> fieldLinesPub;

    /**
     * Constructs a GeometryModule
     */
    public GeometryModule() {
        geoSub = new MQSubscriber<>("vision", "geometry");
        fieldSizePub = new FieldPublisher<>("geometry", "fieldSize", null);
        fieldLinesPub = new FieldPublisher<>("geometry", "fieldLines", null);
    }

    public void run() {
        try {
            subscribe();

            while (true) {
                processGeometry();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribe to publishers
     */
    private void subscribe() {
        try {
            geoSub.subscribe(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Splits SSL_GeomertyData into field size data and line data.
     * Converts line data into a hashmap of name to line.
     */
    private void processGeometry() {
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
}
