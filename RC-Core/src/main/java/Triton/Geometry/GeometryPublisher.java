package Triton.Geometry;

import Proto.MessagesRobocupSslGeometry.*;

import Triton.Shape.Line2D;
import Triton.Vision.VisionData;

public class GeometryPublisher implements Runnable {

    GeometryData geometry;

    public GeometryPublisher() {
        geometry = new GeometryData();
        GeometryData.publish(geometry);
    }

    public void run() {
        boolean isInit = false;
        while (!isInit) {
            try {
                SSL_GeometryData gd = VisionData.get().getGeometry();
                
                if (gd.getCalibCount() != 0) {
                    geometry.setCameras(gd.getCalibList());
                }
                initFieldGeometry(gd.getField());
                if (!geometry.getField().isEmpty()) {
                    Regions.createRegions();
                }

                isInit = !geometry.getField().isEmpty();
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    void initFieldGeometry(SSL_GeometryFieldSize fieldGeometry) {
        if (fieldGeometry.getFieldLinesCount() > 0) {
            for (SSL_FieldLineSegment line : fieldGeometry.getFieldLinesList()) {
                if (!geometry.getField().lineNameList.contains(line.getName())) {
                    geometry.getField().lineNameList.add(line.getName());
                }

                Line2D l2d = new Line2D((double) (line.getP1().getX()), (double) (line.getP1().getY()),
                        (double) (line.getP2().getX()), (double) (line.getP2().getY()));
                l2d.setName(line.getName());
                l2d.setThickness((double) line.getThickness());
                if (!geometry.getField().lineSegments.containsKey(line.getName())) {
                    geometry.getField().lineSegments.put(line.getName(), l2d);
                }
            }
        }

        if (fieldGeometry.getFieldArcsCount() > 0) {
            geometry.getField().arcList = fieldGeometry.getFieldArcsList();
            geometry.getField().centerCircleRadius = fieldGeometry.getFieldArcsList().get(0).getRadius();
        }

        geometry.getField().fieldLength = fieldGeometry.getFieldLength();
        geometry.getField().fieldWidth = fieldGeometry.getFieldWidth();
        geometry.getField().goalDepth = fieldGeometry.getGoalDepth();
        geometry.getField().goalWidth = fieldGeometry.getGoalWidth();
        geometry.getField().boundaryWidth = fieldGeometry.getBoundaryWidth();
    }

    @Override
    public String toString() {
        return geometry.getField().toString();
    }
}