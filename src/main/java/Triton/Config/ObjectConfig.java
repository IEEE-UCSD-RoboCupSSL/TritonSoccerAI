package Triton.Config;

import Triton.CoreModules.Robot.Team;

/**
 * Config file for various objects
 */
public final class ObjectConfig {
    public static final int ROBOT_COUNT = 6;
    public static final double ROBOT_RADIUS = 90.0;
    public static final double BALL_RADIUS = 45.0 / 2.0;
    public static Team MY_TEAM = Team.BLUE;
    public static int MAX_QUEUE_CAPACITY = 5;
}