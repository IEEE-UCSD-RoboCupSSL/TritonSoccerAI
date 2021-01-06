package Triton.Config;

/**
 * Config file for pathfinding
 */
public class PathfinderConfig {
    // Theta* / JPS
    public static final int NODE_RADIUS = 30;
    public static final int NODE_DIAMETER = NODE_RADIUS * 2;
    public static final double ADD_DIST = 0;
    public static final double SAFE_DIST = PathfinderConfig.NODE_RADIUS + ObjectConfig.ROBOT_RADIUS
            + PathfinderConfig.ADD_DIST;
    public static final double BOUNDARY_EXTENSION = 800;

    public static final double SPRINT_TO_ROTATE_DIST_THRESH = 1000;
    public static final double MOVE_ANGLE_THRESH = 20;
    public static final double RD_SWITCH_ROTATE_ANGLE_THRESH = 10;
    public static final double OVERSHOOT_DIST = 3000;
    public static final double BALL_CATCH_DIST = 500;
}