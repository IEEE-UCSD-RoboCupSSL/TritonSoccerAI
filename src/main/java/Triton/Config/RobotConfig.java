package Triton.Config;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.linsol.svd.SolvePseudoInverseSvd_DDRM;
import org.ejml.simple.SimpleMatrix;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class RobotConfig implements IniConfig {
    // initialized with default values, these values are subject to change based on config files &/ cli args
    /* unit: degree */
    public double frontIncAngle = 110;
    public double backIncAngle = 110;

    /* unit: meter */
    public double robotRadius = 0.09;
    public double robotHeight = 0.123;
    public double wheelRadius = 0.025;
    public double dribblerWidth = 0.065;
    public double shootRadius = 0.0715;
    public double centerToDribbler = 0.078;

    /* unit: kg */
    public double robotMass = 2.7;

    /* dynamics specs */
    public int driveGearTeeth = 40; // teeth number
    public int drivenGearTeeth = 12; // teeth number
    public double motorMaxSpeed = 416; // rpm (rev per min)
    public double motorMaxTorque = 1; // N*m

    /* computed dynamics specs */
    public double gearRatio;
    public double wheelMaxSpeed; // rpm
    public double wheelMaxTorque; // N*m
    public double wheelMaxLinearSpeed; // m/s
    public double wheelMaxLinearAcceleration; // m/s^2
    public double robotMaxAbsoluteLinearSpeed; // m/s
    public double robotMaxStableLinearSpeed;
    public double robotMaxVerticalSpeed; // m/s
    public double robotMaxHorizontalSpeed; // m/s
    public double robotMaxAcceleration; // m/s^2
    public double robotMaxAngularSpeed; // rad/s
    public double robotMaxAngularAcceleration; // rad/s^2

    public SimpleMatrix wheelToBodyTransform;
    public SimpleMatrix bodyToWheelTransform;








    public void processFromParsingIni(File iniFIle) throws IOException {
        Wini iniParser = new Wini(iniFIle);
        frontIncAngle = iniParser.get("basic-specs", "front-included-angle", double.class);
        backIncAngle = iniParser.get("basic-specs", "back-included-angle", double.class);
        robotRadius = iniParser.get("basic-specs", "robot-radius", double.class);
        robotHeight = iniParser.get("basic-specs", "robot-height", double.class);
        wheelRadius = iniParser.get("basic-specs", "wheel-radius", double.class);
        dribblerWidth = iniParser.get("basic-specs", "dribbler-width", double.class);
        shootRadius = iniParser.get("basic-specs", "shoot-radius", double.class);
        centerToDribbler = iniParser.get("basic-specs", "center-to-dribbler", double.class);
        robotMass = iniParser.get("basic-specs", "mass", double.class);

        driveGearTeeth = iniParser.get("dynamics-specs", "drive-gear-teeth-num", int.class);
        drivenGearTeeth = iniParser.get("dynamics-specs", "driven-gear-teeth-num", int.class);
        motorMaxSpeed = iniParser.get("dynamics-specs", "motor-max-speed", double.class);
        motorMaxTorque = iniParser.get("dynamics-specs", "motor-max-torque", double.class);

        calculateDynamicsSpecs();
    }


    private void calculateDynamicsSpecs() {
        gearRatio = ((double)drivenGearTeeth / (double)driveGearTeeth);
        wheelMaxSpeed = motorMaxSpeed / gearRatio ;
        wheelMaxTorque = motorMaxTorque * gearRatio;
        wheelMaxLinearSpeed = ((wheelMaxSpeed / 60.0) * 2 * Math.PI) * wheelRadius;

        double theta = Math.toRadians(frontIncAngle / 2);
        double phi = Math.toRadians(backIncAngle / 2);
        wheelToBodyTransform = new SimpleMatrix(new double[][]{
                new double[]{Math.cos(theta)/2.0, -Math.cos(phi)/2, -Math.cos(phi)/2, Math.cos(theta)/2},
                new double[]{Math.sin(theta)/2.0, Math.sin(phi)/2, -Math.sin(phi)/2, -Math.sin(theta)/2},
                new double[]{-1.0 / (4 * robotRadius), -1.0 / (4 * robotRadius), -1.0 / (4 * robotRadius), -1.0 / (4 * robotRadius)}
        });
        DMatrixRMaj wtb = wheelToBodyTransform.copy().getMatrix();
        DMatrixRMaj btw = wheelToBodyTransform.copy().getMatrix();;
        SolvePseudoInverseSvd_DDRM moorePenrosePseudoInverseSolver = new SolvePseudoInverseSvd_DDRM(3, 4);
        moorePenrosePseudoInverseSolver.setA(wtb);
        moorePenrosePseudoInverseSolver.invert(btw);
        bodyToWheelTransform = SimpleMatrix.wrap(btw);

        /* wheel vec : <vLF, vLB, vRB, vRF> , note: counter-clockwise rotation corresponds to positive omega */
        SimpleMatrix wheelVecNorth = new SimpleMatrix(new double[][]{new double[]{100, 100, -100, -100}}).transpose();
        SimpleMatrix wheelVecWest = new SimpleMatrix(new double[][]{new double[]{-100, 100, 100, -100}}).transpose();
        SimpleMatrix wheelVecSouth = new SimpleMatrix(new double[][]{new double[]{-100, -100, 100, 100}}).transpose();
        SimpleMatrix wheelVecEast = new SimpleMatrix(new double[][]{new double[]{100, -100, -100, 100}}).transpose();
        SimpleMatrix wheelVecClockwise = new SimpleMatrix(new double[][]{new double[]{6.28, 6.28, 6.28, 6.28}}).transpose();
        SimpleMatrix wheelVecCounterClockwise = new SimpleMatrix(new double[][]{new double[]{-100, -100, -100, -100}}).transpose();
//        System.out.println("WheelToBody: North : " + wheelToBodyTransform.mult(wheelVecNorth));
//        System.out.println("WheelToBody: West : " + wheelToBodyTransform.mult(wheelVecWest));
//        System.out.println("WheelToBody: South : " + wheelToBodyTransform.mult(wheelVecSouth));
//        System.out.println("WheelToBody: East : " + wheelToBodyTransform.mult(wheelVecEast));
//        System.out.println("WheelToBody: CounterClockwise : " + wheelToBodyTransform.mult(wheelVecCounterClockwise));
//        System.out.println("WheelToBody: Clockwise : " + wheelToBodyTransform.mult(wheelVecClockwise));
//        System.out.println();

        /* Body vec : <vx, vy, omega> */
        SimpleMatrix bodyVecNorth = new SimpleMatrix(new double[][]{new double[]{0, 100, 0}}).transpose();
        SimpleMatrix bodyVecWest = new SimpleMatrix(new double[][]{new double[]{-100, 0, 0}}).transpose();
        SimpleMatrix bodyVecSouth = new SimpleMatrix(new double[][]{new double[]{0, -100, 0}}).transpose();
        SimpleMatrix bodyVecEast = new SimpleMatrix(new double[][]{new double[]{100, 0, 0}}).transpose();
        SimpleMatrix bodyVecCounterClockwise = new SimpleMatrix(new double[][]{new double[]{0, 0, 100}}).transpose();
        SimpleMatrix bodyVecClockwise = new SimpleMatrix(new double[][]{new double[]{0, 0, -100}}).transpose();
//        System.out.println("BodyToWheel: North : " + bodyToWheelTransform.mult(bodyVecNorth));
//        System.out.println("BodyToWheel: West : " + bodyToWheelTransform.mult(bodyVecWest));
//        System.out.println("BodyToWheel: South : " + bodyToWheelTransform.mult(bodyVecSouth));
//        System.out.println("BodyToWheel: East : " + bodyToWheelTransform.mult(bodyVecEast));
//        System.out.println("BodyToWheel: CounterClockwise : " + bodyToWheelTransform.mult(bodyVecCounterClockwise));
//        System.out.println("BodyToWheel: Clockwise : " + bodyToWheelTransform.mult(bodyVecClockwise));
//

        SimpleMatrix wNorthMax = new SimpleMatrix(new double[][]{new double[]{wheelMaxLinearSpeed, wheelMaxLinearSpeed, -wheelMaxLinearSpeed, -wheelMaxLinearSpeed}}).transpose();
        SimpleMatrix wEastMax = new SimpleMatrix(new double[][]{new double[]{wheelMaxLinearSpeed, -wheelMaxLinearSpeed, -wheelMaxLinearSpeed, wheelMaxLinearSpeed}}).transpose();
        SimpleMatrix wCounterClockwiseMax = new SimpleMatrix(new double[][]{new double[]{-wheelMaxLinearSpeed, -wheelMaxLinearSpeed, -wheelMaxLinearSpeed, -wheelMaxLinearSpeed}}).transpose();

        robotMaxVerticalSpeed = wheelToBodyTransform.mult(wNorthMax).get(1, 0);
        robotMaxHorizontalSpeed = wheelToBodyTransform.mult(wEastMax).get(0, 0);
        robotMaxAbsoluteLinearSpeed = Math.max(robotMaxVerticalSpeed, robotMaxHorizontalSpeed);
        robotMaxStableLinearSpeed = Math.min(robotMaxVerticalSpeed, robotMaxHorizontalSpeed);
        robotMaxAngularSpeed = wheelToBodyTransform.mult(wCounterClockwiseMax).get(2, 0);



        wheelMaxLinearAcceleration = (wheelMaxTorque / wheelRadius) / robotMass;
        SimpleMatrix wNorthAccMax = new SimpleMatrix(new double[][]{new double[]{wheelMaxLinearAcceleration, wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration}}).transpose();
        SimpleMatrix wEastAccMax = new SimpleMatrix(new double[][]{new double[]{wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration, wheelMaxLinearAcceleration}}).transpose();
        SimpleMatrix wCounterClockwiseAccMax = new SimpleMatrix(new double[][]{new double[]{-wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration, -wheelMaxLinearAcceleration}}).transpose();
        robotMaxAcceleration = Math.max(wheelToBodyTransform.mult(wNorthAccMax).get(1, 0),
                                        wheelToBodyTransform.mult(wEastAccMax).get(0, 0));
        robotMaxAngularAcceleration = wheelToBodyTransform.mult(wCounterClockwiseAccMax).get(2, 0);





    }

    @Override
    public String toString() {
        return "RobotConfig{" +
                "frontIncAngle=" + frontIncAngle +
                ", backIncAngle=" + backIncAngle +
                ", robotRadius=" + robotRadius +
                ", robotHeight=" + robotHeight +
                ", wheelRadius=" + wheelRadius + "\n" +
                "dribblerWidth=" + dribblerWidth +
                ", shootRadius=" + shootRadius +
                ", centerToDribbler=" + centerToDribbler + "\n" +
                "robotMass=" + robotMass +
                ", driveGearTeeth=" + driveGearTeeth +
                ", drivenGearTeeth=" + drivenGearTeeth + "\n" +
                "motorMaxSpeed=" + motorMaxSpeed +
                ", motorMaxTorque=" + motorMaxTorque +
                ", gearRatio=" + gearRatio + "\n" +
                "wheelMaxSpeed=" + wheelMaxSpeed + "\n" +
                "wheelMaxTorque=" + wheelMaxTorque + "\n" +
                "wheelMaxLinearSpeed=" + wheelMaxLinearSpeed + " m/s\n" +
                "wheelMaxLinearAcceleration=" + wheelMaxLinearAcceleration + " m/s^2\n" +
                "robotMaxAbsoluteLinearSpeed=" + robotMaxAbsoluteLinearSpeed + " m/s\n" +
                "robotMaxStableLinearSpeed=" + robotMaxStableLinearSpeed + " m/s\n" +
                "robotVerticalMaxSpeed=" + robotMaxVerticalSpeed + " m/s\n" +
                "robotHorizontalMaxSpeed=" + robotMaxHorizontalSpeed + " m/s\n" +
                "robotMaxAcceleration=" + robotMaxAcceleration + " m/s^2 \n" +
                "robotMaxAngularSpeed=" + robotMaxAngularSpeed + " rad/s\n" +
                "robotMaxAngularAcceleration=" + robotMaxAngularAcceleration + " rad/s^2\n" +
                '}';
    }
}
