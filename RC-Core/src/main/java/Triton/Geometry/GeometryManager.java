package Triton.Geometry;

import java.util.List;
import Proto.MessagesRobocupSslGeometry.*;

import Triton.Shape.Line2D;

public class GeometryManager {

    public List<SSL_GeometryCameraCalibration> cameras;
    public Field field = new Field();
    public boolean isInit = false;

    public Field getFieldStaticObjects() {
        return field;
    }

    public boolean init(SSL_GeometryData gd) {
        if (gd.getCalibCount() != 0) {
            cameras = gd.getCalibList();
        }

        initFieldGeometry(gd.getField());
        if(!field.isEmpty()) {
            Regions.createRegions(this);
        }
        return !field.isEmpty();
    }

    void initFieldGeometry(SSL_GeometryFieldSize fieldGeometry) {
        if(fieldGeometry.getFieldLinesCount() > 0) { 
            for(SSL_FieldLineSegment line: fieldGeometry.getFieldLinesList()) {
                if(!field.lineNameList.contains(line.getName())) {
                    field.lineNameList.add(line.getName());
                }
                
                Line2D l2d = new Line2D((double)(line.getP1().getX()), 
                                        (double)(line.getP1().getY()),
                                        (double)(line.getP2().getX()),
                                        (double)(line.getP2().getY()) );
                l2d.setName(line.getName());
                l2d.setThickness((double)line.getThickness());
                if(!field.lineSegments.containsKey(line.getName())) {
                    field.lineSegments.put(line.getName(), l2d);
                }
            }
        }

        if(fieldGeometry.getFieldArcsCount() > 0) {
            field.arcList = fieldGeometry.getFieldArcsList();
            field.centerCircleRadius = fieldGeometry.getFieldArcsList().get(0).getRadius();
        }

        field.fieldLength = fieldGeometry.getFieldLength();
        field.fieldWidth = fieldGeometry.getFieldWidth();
        field.goalDepth = fieldGeometry.getGoalDepth();
        field.goalWidth = fieldGeometry.getGoalWidth();
        field.boundaryWidth = fieldGeometry.getBoundaryWidth();
    }

    public float getCameraQ0(int cameraID) {
        return this.cameras.get(cameraID).getQ0();
    }

    @Override
    public String toString() {
        return field.toString();
    }
}