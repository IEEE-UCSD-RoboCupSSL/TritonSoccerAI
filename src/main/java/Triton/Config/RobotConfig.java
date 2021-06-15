package Triton.Config;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class RobotConfig implements IniConfig {
    // initialized with default values, these values are subject to change based on config files &/ cli args
    /* unit: degree */
    double frontIncAngle = 110;
    double backIncAngle = 110;

    /* unit: meter */
    double robotRadius = 0.09;
    double robotHeight = 0.123;
    double wheelRadius = 0.025;
    double dribblerWidth = 0.065;
    double shootRadius = 0.0715;
    double centerToDribbler = 0.078;

    /* unit: kg */
    double robotMass = 2.7;

    /* dynamics specs */
    int driveGearTeeth = 40; // teeth number
    int drivenGearTeeth = 12; // teeth number
    double motorMaxSpeed = 416; // rpm (rev per min)
    double motorMaxTorque = 1; // N*m

    /* computed dynamics specs */
    double gearRatio;
    double wheelMaxSpeed; // rpm
    double wheelMaxTorque; // N*m
    double wheelMaxLinearSpeed; // m/s
    double robotMaxSpeed; // m/s
    double robotMaxAcceleration; // m/s^2
    double robotMaxAngularSpeed; // rad/s
    double robotMaxAngularAcceleration; // rad/s^2






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
    }


    private void calculateDynamicsSpecs() {
        gearRatio = ((double)drivenGearTeeth / (double)driveGearTeeth);
        wheelMaxSpeed = motorMaxSpeed / gearRatio ;
        wheelMaxTorque = motorMaxTorque * gearRatio;
        wheelMaxLinearSpeed = ((wheelMaxSpeed / 60.0) * 2 * Math.PI) * wheelRadius;

    }



}
