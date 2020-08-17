package Triton.Geometry;

import Proto.MessagesRobocupSslGeometry.*;

import Triton.Shape.Line2D;
import Triton.Vision.VisionData;

public class GeometryPublisher implements Runnable {

    GeometryData geometry = new GeometryData();

    public void run() {
        boolean isInit = false;
        while (!isInit) {
            try {
                SSL_GeometryData gd = VisionData.get().getGeometry();
                
                if (gd.getCalibCount() != 0) {
                    geometry.setCameras(gd.getCalibList());
                }
                Field field = new Field();
                initFieldGeometry(gd.getField(), field);
                geometry.setField(field);
                if (!geometry.getField().isEmpty()) {
                    Regions.createRegions();
                }

                isInit = !geometry.getField().isEmpty();
                geometry.publish();
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    void initFieldGeometry(SSL_GeometryFieldSize fieldGeometry, Field field) {
        if (fieldGeometry.getFieldLinesCount() > 0) {
            for (SSL_FieldLineSegment line : fieldGeometry.getFieldLinesList()) {
                if (!field.lineNameList.contains(line.getName())) {
                    field.lineNameList.add(line.getName());
                }

                Line2D l2d = new Line2D((double) (line.getP1().getX()), (double) (line.getP1().getY()),
                        (double) (line.getP2().getX()), (double) (line.getP2().getY()));
                l2d.setName(line.getName());
                l2d.setThickness((double) line.getThickness());
                if (!field.lineSegments.containsKey(line.getName())) {
                    field.lineSegments.put(line.getName(), l2d);
                }
            }
        }

        if (fieldGeometry.getFieldArcsCount() > 0) {
            field.arcList = fieldGeometry.getFieldArcsList();
            field.centerCircleRadius = fieldGeometry.getFieldArcsList().get(0).getRadius();
        }

        field.fieldLength = fieldGeometry.getFieldLength();
        field.fieldWidth = fieldGeometry.getFieldWidth();
        field.goalDepth = fieldGeometry.getGoalDepth();
        field.goalWidth = fieldGeometry.getGoalWidth();
        field.boundaryWidth = fieldGeometry.getBoundaryWidth();
    }

    @Override
    public String toString() {
        return geometry.getField().toString();
    }
}