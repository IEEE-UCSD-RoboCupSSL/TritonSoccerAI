package Triton.Config;

public class PathfinderConfig {
    // Vector Based
    public static final double PUSH_STRENGTH = 4000000;
    public static final double PULL_STRENGTH = 100;

    // A*
    public static final int NODE_RADIUS = 50;
    public static final int NODE_DIAMETER = NODE_RADIUS * 2;
    public static final double ADD_DIST = 20;
    public static final double SAFE_DIST = PathfinderConfig.NODE_RADIUS + ObjectConfig.ROBOT_RADIUS
                                + PathfinderConfig.ADD_DIST;
    public static final int NEIGHBOR_DIST = 2;
}