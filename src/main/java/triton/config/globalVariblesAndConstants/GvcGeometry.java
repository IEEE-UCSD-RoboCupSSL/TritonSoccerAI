package triton.config.globalVariblesAndConstants;

import triton.misc.math.geometry.Circle2D;
import triton.misc.math.geometry.Line2D;
import triton.misc.math.linearAlgebra.Vec2D;

import java.util.HashMap;

public class GvcGeometry {
    static {
        FIELD_LINES = new HashMap<>();
        initGeo();
    }

    public static boolean IS_GEO_INIT = false;

    // Field Lines
    public static HashMap<String, Line2D> FIELD_LINES;
    public static Line2D TOP_TOUCH_LINE = new Line2D(new Vec2D(-4495.0, 2995.0), new Vec2D(4495.0, 2995.0));
    public static Line2D BOTTOM_TOUCH_LINE = new Line2D(new Vec2D(-4495.0, -2995.0), new Vec2D(4495.0, -2995.0));
    public static Line2D LEFT_GOAL_LINE = new Line2D(new Vec2D(-4490.0, -2995.0), new Vec2D(-4490.0, 2995.0));
    public static Line2D RIGHT_GOAL_LINE = new Line2D(new Vec2D(4490.0, -2995.0), new Vec2D(4490.0, 2995.0));
    public static Line2D HALFWAY_LINE = new Line2D(new Vec2D(0.0, -2995.0), new Vec2D(0.0, 2995.0));
    public static Line2D CENTER_LINE = new Line2D(new Vec2D(-4490.0, 0.0), new Vec2D(4490.0, 0.0));
    public static Line2D LEFT_PENALTY_STRETCH = new Line2D(new Vec2D(-3495.0, -1000.0), new Vec2D(-3495.0, 1000.0));
    public static Line2D RIGHT_PENALTY_STRETCH = new Line2D(new Vec2D(3495.0, -1000.0), new Vec2D(3495.0, 1000.0));
    public static Line2D RIGHT_GOAL_TOP_LINE = new Line2D(new Vec2D(4490.0, 500.0), new Vec2D(4690.0, 500.0));
    public static Line2D RIGHT_GOAL_BOTTOM_LINE = new Line2D(new Vec2D(4490.0, -500.0), new Vec2D(4690.0, -500.0));
    public static Line2D RIGHT_GOAL_DEPTH_LINE = new Line2D(new Vec2D(4685.0, -500.0), new Vec2D(4685.0, 500.0));
    public static Line2D LEFT_GOAL_TOP_LINE = new Line2D(new Vec2D(-4490.0, 500.0), new Vec2D(-4690.0, 500.0));
    public static Line2D LEFT_GOAL_BOTTOM_LINE = new Line2D(new Vec2D(-4490.0, -500.0), new Vec2D(-4690.0, -500.0));
    public static Line2D LEFT_GOAL_DEPTH_LINE = new Line2D(new Vec2D(-4685.0, -500.0), new Vec2D(-4685.0, 500.0));
    public static Line2D LEFT_FIELD_LEFT_PENALTY_STRETCH = new Line2D(new Vec2D(-4490.0, 1000.0), new Vec2D(-3490.0, 1000.0));
    public static Line2D LEFT_FIELD_RIGHT_PENALTY_STRETCH = new Line2D(new Vec2D(-4490.0, -1000.0), new Vec2D(-3490.0, -1000.0));
    public static Line2D RIGHT_FIELD_LEFT_PENALTY_STRETCH = new Line2D(new Vec2D(4490.0, -1000.0), new Vec2D(3490.0, -1000.0));
    public static Line2D RIGHT_FIELD_RIGHT_PENALTY_STRETCH = new Line2D(new Vec2D(4490.0, 1000.0), new Vec2D(3490.0, 1000.0));

    // Field Circle
    public static Vec2D FIELD_CIRCLE_CENTER = new Vec2D(0, 0);
    public static double FIELD_CIRCLE_RADIUS = 400;
    public static Circle2D FIELD_CIRCLE = new Circle2D(FIELD_CIRCLE_CENTER, FIELD_CIRCLE_RADIUS);

    // Field Size
    public static double FIELD_WIDTH = 6000;
    public static double FIELD_LENGTH = 9000;
    public static Vec2D FIELD_BOTTOM_LEFT = new Vec2D(-FIELD_LENGTH / 2, -FIELD_WIDTH / 2);
    public static double GOAL_LEFT = -500;
    public static double GOAL_RIGHT = 500;
    public static double GOAL_LENGTH = 1000;
    public static double GOAL_DEPTH = 200;
    public static double FULL_FIELD_LENGTH = FIELD_LENGTH + 2 * GOAL_DEPTH;

    // Other
    public static Vec2D GOAL_CENTER_TEAM = new Vec2D(0, -FIELD_LENGTH / 2);
    public static Vec2D GOAL_CENTER_FOE = new Vec2D(0, FIELD_LENGTH / 2);
    public static double PENALTY_STRETCH_WIDTH =
            LEFT_FIELD_LEFT_PENALTY_STRETCH.p1.y - LEFT_FIELD_RIGHT_PENALTY_STRETCH.p1.y;
    public static double PENALTY_STRETCH_LEFT = -PENALTY_STRETCH_WIDTH / 2;
    public static double PENALTY_STRETCH_RIGHT = PENALTY_STRETCH_WIDTH / 2;
    public static double PENALTY_STRETCH_DEPTH =
            LEFT_FIELD_LEFT_PENALTY_STRETCH.p2.x - LEFT_FIELD_LEFT_PENALTY_STRETCH.p1.x;
    public static double PENALTY_STRETCH_Y = -FIELD_LENGTH / 2 + PENALTY_STRETCH_DEPTH;

    public static void initGeo() {
        FIELD_LINES.put("TopTouchLine", TOP_TOUCH_LINE);
        FIELD_LINES.put("BottomTouchLine", BOTTOM_TOUCH_LINE);
        FIELD_LINES.put("LeftGoalLine", LEFT_GOAL_LINE);
        FIELD_LINES.put("RightGoalLine", RIGHT_GOAL_LINE);
        FIELD_LINES.put("HalfwayLine", HALFWAY_LINE);
        FIELD_LINES.put("CenterLine", CENTER_LINE);
        FIELD_LINES.put("LeftPenaltyStretch", LEFT_PENALTY_STRETCH);
        FIELD_LINES.put("RightPenaltyStretch", RIGHT_PENALTY_STRETCH);
        FIELD_LINES.put("RightGoalTopLine", RIGHT_GOAL_TOP_LINE);
        FIELD_LINES.put("RightGoalBottomLine", RIGHT_GOAL_BOTTOM_LINE);
        FIELD_LINES.put("RightGoalDepthLine", RIGHT_GOAL_DEPTH_LINE);
        FIELD_LINES.put("LeftGoalTopLine", LEFT_GOAL_TOP_LINE);
        FIELD_LINES.put("LeftGoalBottomLine", LEFT_GOAL_BOTTOM_LINE);
        FIELD_LINES.put("LeftGoalDepthLine", LEFT_GOAL_DEPTH_LINE);
        FIELD_LINES.put("LeftFieldLeftPenaltyStretch", LEFT_FIELD_LEFT_PENALTY_STRETCH);
        FIELD_LINES.put("LeftFieldRightPenaltyStretch", LEFT_FIELD_RIGHT_PENALTY_STRETCH);
        FIELD_LINES.put("RightFieldLeftPenaltyStretch", RIGHT_FIELD_LEFT_PENALTY_STRETCH);
        FIELD_LINES.put("RightFieldRightPenaltyStretch", RIGHT_FIELD_RIGHT_PENALTY_STRETCH);
    }
}
