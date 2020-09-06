package Triton.Config;

public class PathfinderConfig {
    // Vector Based
    public static final double PUSH_STRENGTH = 4000000;
    public static final double PULL_STRENGTH = 100;

    // A*
    public static final int NODE_RADIUS = 100;
    public static final int NODE_DIAMETER = NODE_RADIUS * 2;
    public static final double SAFETY_DIST = 10;
}