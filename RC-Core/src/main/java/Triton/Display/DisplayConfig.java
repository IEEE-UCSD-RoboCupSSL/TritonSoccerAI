package Triton.Display;

import Triton.Detection.*;
public class DisplayConfig {
    public static final double SCALE = 1.0 / 10.0;
    public static final int ROBOT_RADIUS_PIXELS = (int) (ObjectParams.ROBOT_RADIUS * SCALE);
    public static final int BALL_RADIUS_PIXELS = (int) (ObjectParams.BALL_RADIUS * SCALE);
    public static final int ROBOT_OUTLINE_THICKNESS = 1;
    public static final int BALL_OUTLINE_THICKNESS = 1;
}