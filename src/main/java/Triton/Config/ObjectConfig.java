package Triton.Config;

import Triton.CoreModules.Robot.Team;

/**
 * Config file for various objects
 */
public final class ObjectConfig {
    public static final int ROBOT_COUNT = 6;
    public static final double ROBOT_RADIUS = 90.0;
    public static final double BALL_RADIUS = 45.0 / 2.0;

    public static final double EXCESSIVE_DRIBBLING_DIST = 1000; // 1 meter
    public static final double POS_PRECISION = 50; // +-10mm tolerance
    public static final double DIR_PRECISION = 10; // +-10degree tolerance
    public static final double MAX_KICK_VEL = 6.5;


    public static Team MY_TEAM = Team.BLUE;
    public static int MAX_POS_LIST_CAPACITY = 15;
    public static double DRIBBLER_OFFSET = 105;
}