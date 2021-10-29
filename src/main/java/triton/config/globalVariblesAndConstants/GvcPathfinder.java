package triton.config.globalVariblesAndConstants;

import triton.config.oldConfigs.ObjectConfig;

import static triton.config.oldConfigs.ObjectConfig.BALL_RADIUS;
import static triton.config.oldConfigs.ObjectConfig.ROBOT_MIN_RADIUS;

/**
 * Config file for pathfinding
 */
public class GvcPathfinder {
    // Theta* / JPS
    public static final int NODE_RADIUS = 30;
    public static final int NODE_DIAMETER = NODE_RADIUS * 2;
    public static final double ADD_DIST = 5;
    public static final double SAFE_DIST = GvcPathfinder.NODE_RADIUS + ObjectConfig.ROBOT_RADIUS
            + GvcPathfinder.ADD_DIST;
    public static final double BOUNDARY_EXTENSION = 800;

    public static final double SPRINT_TO_ROTATE_DIST_THRESH = 500; // When sprinting, face closest node outside of this threshold
    public static final double RD_ANGLE_THRESH = 90; // Outside angle threshold, use RV, inside angle, use RD
    public static final double MOVE_ANGLE_THRESH = 40; // Don't move if outside angle threshold
    public static final double AUTOCAP_DIST_THRESH = 1000; // For getBall, Turn on autocap if ball is within threshold



    public static final double DRIB_ROTATE_BALL_PUSH = 5000;
    public static final double DRIB_ROTATE_DIST = ROBOT_MIN_RADIUS + BALL_RADIUS;
    public static final double DRIB_ROTATE_MAX_DIST = (ROBOT_MIN_RADIUS + BALL_RADIUS) * 2;
}