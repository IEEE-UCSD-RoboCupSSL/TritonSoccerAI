package Triton.Config;

/**
 * Config file for pathfinding
 */
public class PathfinderConfig {
    // Theta* / JPS
    public static final int NODE_RADIUS = 30;
    public static final int NODE_DIAMETER = NODE_RADIUS * 2;
    public static final double ADD_DIST = 10;
    public static final double SAFE_DIST = PathfinderConfig.NODE_RADIUS + ObjectConfig.ROBOT_RADIUS
            + PathfinderConfig.ADD_DIST;
    public static final double BOUNDARY_EXTENSION = 800;

    public static final double SPRINT_TO_ROTATE_DIST_THRESH = 500; // When sprinting, face closest node outside of this threshold
    public static final double RD_ANGLE_THRESH = 80; // Outside angle threshold, use RV, inside angle, use RD
    public static final double MOVE_ANGLE_THRESH = 40; // Don't move if outside angle threshold
    public static final double AUTOCAP_DIST_THRESH = 200; // For getBall, Turn on autocap if ball is within threshold
}