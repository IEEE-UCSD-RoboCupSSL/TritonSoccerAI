package Triton.Config;

/**
 * Config file for display
 */
public final class DisplayConfig {
    public static final double SCALE = 1.0 / 13.0;
    public static final int TARGET_FPS = 1000;
    public static final long UPDATE_DELAY = 1000 / TARGET_FPS; // ms
    public static final int ROBOT_RADIUS_PIXELS = (int) (ObjectConfig.ROBOT_RADIUS * SCALE);
    public static final int BALL_RADIUS_PIXELS = (int) (ObjectConfig.BALL_RADIUS * SCALE);
    public static final int ROBOT_OUTLINE_THICKNESS = 1;
    public static final int BALL_OUTLINE_THICKNESS = 1;
}