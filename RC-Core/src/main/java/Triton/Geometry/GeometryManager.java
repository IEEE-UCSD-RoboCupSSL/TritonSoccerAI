package Triton.Geometry;

import java.util.List;
import Proto.MessagesRobocupSslGeometry.*;

import Triton.Shape.Line2D;

public class GeometryManager {

    public Camera cam = new Camera();
    public Field field = new Field();
    public boolean isInit = false;

    List<SSL_FieldCicularArc> tmp2;

    public Camera getCameraCalibrationSetting() {
        return cam;
    }

    public Field getFieldStaticObjects() {
        return field;
    }

    public boolean init(SSL_GeometryData gd) {
        // initCameraCalibration(gd.getCalib(0));
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
            tmp2 = fieldGeometry.getFieldArcsList();
            field.centerCircleRadius = fieldGeometry.getFieldArcsList().get(0).getRadius();
        }

        field.fieldLength = fieldGeometry.getFieldLength();
        field.fieldWidth = fieldGeometry.getFieldWidth();
        field.goalDepth = fieldGeometry.getGoalDepth();
        field.goalWidth = fieldGeometry.getGoalWidth();
        field.boundaryWidth = fieldGeometry.getBoundaryWidth();
    }

    void initCameraCalibration(SSL_GeometryCameraCalibration camCali) {
        this.cam.cameraID = camCali.getCameraId();
        this.cam.focalLength = camCali.getFocalLength();
        this.cam.principalPointX = camCali.getPrincipalPointX();
        this.cam.principalPointY = camCali.getPrincipalPointY();
        this.cam.distortion = camCali.getDistortion();
        this.cam.q0 = camCali.getQ0();
        this.cam.q1 = camCali.getQ1();
        this.cam.q2 = camCali.getQ2();
        this.cam.q3 = camCali.getQ3();
        this.cam.tx = camCali.getTx();
        this.cam.ty = camCali.getTy();
        this.cam.tz = camCali.getTz();
    }

    @Override
    public String toString() {
        String s = field.toString();
        
        for(SSL_FieldCicularArc arc : tmp2) {
            s += arc.getName() + ": ";
            s += "center: (" + arc.getCenter().getX() + ", " + arc.getCenter().getY()
              + ")  radius: " + arc.getRadius() + "  start/end angles: (" 
              + arc.getA1() + ", " + arc.getA2() + ") in radians \n";
        }

        s += "====================================================\n";
        return s;
    }
}