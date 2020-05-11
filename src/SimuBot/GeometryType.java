package SimuBot;
import java.util.List;
import Protobuf.MessagesRobocupSslGeometry.*;
import Protobuf.MessagesRobocupSslGeometry.SSL_GeometryFieldSize.*;



public class GeometryType extends AbstractData {
    public class Camera {
        public int cameraID;
        public float focalLength;
        public float principalPointX;
        public float principalPointY;
        public float distortion;
        public float q0;
        public float q1;
        public float q2;
        public float q3;
        public float tx;
        public float ty;
        public float tz;
        public float derivedCameraWorldTx;
        public float derivedCameraWorldTy;
        public float derivedCameraWorldTz;

        // To-Do in the future: add math methods to convert the above into neat matrix representation
    }

    public class Field {
        public int fieldLength;
        public int fieldWidth;
        public int goalWidth;
        public int goalDepth;
        public int boundaryWidth;
    }

    private static final GeometryType GEOMETRY = new GeometryType();   
    public Camera cam = new Camera();
    public Field field = new Field();

    public GeometryType() {}

    public static GeometryType getInstance() {
        return GEOMETRY;
    }

    public Camera getCameraCalibrationSetting() {
        return cam;
    }

    public Field getFieldStaticObjects() {
        return field;
    }

    List<SSL_FieldLineSegment> tmp;

    void updateFieldGeometry(SSL_GeometryFieldSize fieldGeometry) {
        // System.out.println("Num of Line Segs = " + fieldGeometry.getFieldLinesCount());
        if(fieldGeometry.getFieldLinesCount() > 0) { 
            tmp = fieldGeometry.getFieldLinesList();
        }
        field.fieldLength = fieldGeometry.getFieldLength();
        field.fieldWidth = fieldGeometry.getFieldWidth();
        field.goalDepth = fieldGeometry.getGoalDepth();
        field.goalWidth = fieldGeometry.getGoalWidth();
        field.boundaryWidth = fieldGeometry.getBoundaryWidth();
    }

    void updateCameraCalibration(SSL_GeometryCameraCalibration camCali) {
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
        String s = "";
        /*
        s += "[[Camera Calibration]]=============================\n";
        s += "Camera ID = " + cam.cameraID + "\n";
        s += "Focal Lenth = " + cam.focalLength + "\n";
        s += "Principal Point = (" + cam.principalPointX 
                            + ", " + cam.principalPointY + ")\n";
        s += "Distortion = " + cam.distortion + "\n";
        s += "(q0, q1, q2, q3) = (" + cam.q0 + ", " 
                                    + cam.q1 + ", "
                                    + cam.q2 + ", "
                                    + cam.q3 + ")\n";
        s += "(tx, ty, tz) = (" + cam.tx + ", "
                                + cam.ty + ", "
                                + cam.tz + ", \n"; */

        s += "[[Field Configuration]]=============================\n"; 
        s += "Field(length, width) = (" + field.fieldLength + ", " 
                                        + field.fieldWidth + ")\n";
        s += "Goal(depth, width) = (" + field.goalDepth + ", "
                                      + field.goalWidth + ")\n";
        s += "Boundary Width = " + field.boundaryWidth + "\n";

        s += "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n";

        
        System.out.println(tmp.size());
        for(SSL_FieldLineSegment line : tmp) {
            s += line.getName() + ": (" + line.getP1().getX() + ", "
                                        + line.getP1().getY() + ") " 
                                  + "(" + line.getP2().getX() + ", "
                                        + line.getP2().getY() + ") "
                                        + " Thickness: " + line.getThickness() + "\n";
        }

        return s;
    }

    
}

