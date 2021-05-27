package Triton.Config.OldConfigs;

import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Matrix.Vec2D;

import java.util.HashMap;

public class GeometryConfig {
    // Field Size
    public static final double FIELD_WIDTH = 6000;
    public static final double FIELD_LENGTH = 9000;
    public static final Vec2D FIELD_BOTTOM_LEFT = new Vec2D(-FIELD_LENGTH / 2, -FIELD_WIDTH / 2);
    public static final double GOAL_LEFT = -500;
    public static final double GOAL_RIGHT = 500;
    public static final double GOAL_LENGTH = 1000;
    public static final double GOAL_DEPTH = 200;
    public static final double FULL_FIELD_LENGTH = FIELD_LENGTH + 2 * GOAL_DEPTH;

    // Field Lines
    public static final HashMap<String, Line2D> FIELD_LINES = new HashMap<>();
    public static final Line2D TOP_TOUCH_LINE = new Line2D(new Vec2D(-4495.0, 2995.0), new Vec2D(4495.0, 2995.0));
    public static final Line2D BOTTOM_TOUCH_LINE = new Line2D(new Vec2D(-4495.0, -2995.0), new Vec2D(4495.0, -2995.0));
    public static final Line2D LEFT_GOAL_LINE = new Line2D(new Vec2D(-4490.0, -2995.0), new Vec2D(-4490.0, 2995.0));
    public static final Line2D RIGHT_GOAL_LINE = new Line2D(new Vec2D(4490.0, -2995.0), new Vec2D(4490.0, 2995.0));
    public static final Line2D HALFWAY_LINE = new Line2D(new Vec2D(0.0, -2995.0), new Vec2D(0.0, 2995.0));
    public static final Line2D CENTER_LINE = new Line2D(new Vec2D(-4490.0, 0.0), new Vec2D(4490.0, 0.0));
    public static final Line2D LEFT_PENALTY_STRETCH = new Line2D(new Vec2D(-3495.0, -1000.0), new Vec2D(-3495.0, 1000.0));
    public static final Line2D RIGHT_PENALTY_STRETCH = new Line2D(new Vec2D(3495.0, -1000.0), new Vec2D(3495.0, 1000.0));
    public static final Line2D RIGHT_GOAL_TOP_LINE = new Line2D(new Vec2D(4490.0, 500.0), new Vec2D(4690.0, 500.0));
    public static final Line2D RIGHT_GOAL_BOTTOM_LINE = new Line2D(new Vec2D(4490.0, -500.0), new Vec2D(4690.0, -500.0));
    public static final Line2D RIGHT_GOAL_DEPTH_LINE = new Line2D(new Vec2D(4685.0, -500.0), new Vec2D(4685.0, 500.0));
    public static final Line2D LEFT_GOAL_TOP_LINE = new Line2D(new Vec2D(-4490.0, 500.0), new Vec2D(-4690.0, 500.0));
    public static final Line2D LEFT_GOAL_BOTTOM_LINE = new Line2D(new Vec2D(-4490.0, -500.0), new Vec2D(-4690.0, -500.0));
    public static final Line2D LEFT_GOAL_DEPTH_LINE = new Line2D(new Vec2D(-4685.0, -500.0), new Vec2D(-4685.0, 500.0));
    public static final Line2D LEFT_FIELD_LEFT_PENALTY_STRETCH = new Line2D(new Vec2D(-4490.0, 1000.0), new Vec2D(-3490.0, 1000.0));
    public static final Line2D LEFT_FIELD_RIGHT_PENALTY_STRETCH = new Line2D(new Vec2D(-4490.0, -1000.0), new Vec2D(-3490.0, -1000.0));
    public static final Line2D RIGHT_FIELD_LEFT_PENALTY_STRETCH = new Line2D(new Vec2D(4490.0, -1000.0), new Vec2D(3490.0, -1000.0));
    public static final Line2D RIGHT_FIELD_RIGHT_PENALTY_STRETCH = new Line2D(new Vec2D(4490.0, 1000.0), new Vec2D(3490.0, 1000.0));

    // Field Circle
    public static final Vec2D FIELD_CIRCLE_CENTER = new Vec2D(0, 0);
    public static final double FIELD_CIRCLE_RADIUS = 400;
    public static final Circle2D FIELD_CIRCLE = new Circle2D(FIELD_CIRCLE_CENTER, FIELD_CIRCLE_RADIUS);

    public static void initGeo() {
        FIELD_LINES.put("TOP_TOUCH_LINE", TOP_TOUCH_LINE);
        FIELD_LINES.put("BOTTOM_TOUCH_LINE", BOTTOM_TOUCH_LINE);
        FIELD_LINES.put("LEFT_GOAL_LINE", LEFT_GOAL_LINE);
        FIELD_LINES.put("RIGHT_GOAL_LINE", RIGHT_GOAL_LINE);
        FIELD_LINES.put("HALFWAY_LINE", HALFWAY_LINE);
        FIELD_LINES.put("CENTER_LINE", CENTER_LINE);
        FIELD_LINES.put("LEFT_PENALTY_STRETCH", LEFT_PENALTY_STRETCH);
        FIELD_LINES.put("RIGHT_PENALTY_STRETCH", RIGHT_PENALTY_STRETCH);
        FIELD_LINES.put("RIGHT_GOAL_TOP_LINE", RIGHT_GOAL_TOP_LINE);
        FIELD_LINES.put("RIGHT_GOAL_BOTTOM_LINE", RIGHT_GOAL_BOTTOM_LINE);
        FIELD_LINES.put("RIGHT_GOAL_DEPTH_LINE", RIGHT_GOAL_DEPTH_LINE);
        FIELD_LINES.put("LEFT_GOAL_TOP_LINE", LEFT_GOAL_TOP_LINE);
        FIELD_LINES.put("LEFT_GOAL_BOTTOM_LINE", LEFT_GOAL_BOTTOM_LINE);
        FIELD_LINES.put("LEFT_GOAL_DEPTH_LINE", LEFT_GOAL_DEPTH_LINE);
        FIELD_LINES.put("LEFT_FIELD_LEFT_PENALTY_STRETCH", LEFT_FIELD_LEFT_PENALTY_STRETCH);
        FIELD_LINES.put("LEFT_FIELD_RIGHT_PENALTY_STRETCH", LEFT_FIELD_RIGHT_PENALTY_STRETCH);
        FIELD_LINES.put("RIGHT_FIELD_LEFT_PENALTY_STRETCH", RIGHT_FIELD_LEFT_PENALTY_STRETCH);
        FIELD_LINES.put("RIGHT_FIELD_RIGHT_PENALTY_STRETCH", RIGHT_FIELD_RIGHT_PENALTY_STRETCH);
    }
}
