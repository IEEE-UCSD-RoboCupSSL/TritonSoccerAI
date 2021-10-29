package triton.config.oldConfigs;

/**
 * Config file for various objects
 */
public final class ObjectConfig {
    public static double ROBOT_RADIUS = 90.0;
    public static double ROBOT_MIN_RADIUS = ROBOT_RADIUS - 15;
    public static final double BALL_RADIUS = 45.0 / 2.0;

    public static final double EXCESSIVE_DRIBBLING_DIST = 500; // 0.5 meter
    public static final double POS_PRECISION = 50; // +-10mm tolerance
    public static final double DIR_PRECISION = 10; // +-10degree tolerance
    public static double MAX_KICK_VEL = 6.5;

    public static int MAX_POS_LIST_CAPACITY = 15;
    public static double DRIBBLER_OFFSET = 85; // Competition Ad Hoc, Was 105
}