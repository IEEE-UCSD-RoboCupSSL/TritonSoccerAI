package Triton.Config.GlobalVariblesAndConstants;

import Triton.Misc.Math.Geometry.Circle2D;
import Triton.Misc.Math.Geometry.Line2D;
import Triton.Misc.Math.Geometry.Rect2D;
import Triton.Misc.Math.LinearAlgebra.Vec2D;
import org.checkerframework.checker.units.qual.C;

import java.util.HashMap;

import static Triton.Misc.Math.Coordinates.PerspectiveConverter.audienceToPlayer;

public class GvcGeometry {
    static {
        fieldLines = new HashMap<>();
        initGeo();
    }

    public static boolean IS_GEO_INIT = false;

    // Field Lines
    public static HashMap<String, Line2D> fieldLines;
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
        fieldLines.put("TopTouchLine", TOP_TOUCH_LINE);
        fieldLines.put("BottomTouchLine", BOTTOM_TOUCH_LINE);
        fieldLines.put("LeftGoalLine", LEFT_GOAL_LINE);
        fieldLines.put("RightGoalLine", RIGHT_GOAL_LINE);
        fieldLines.put("HalfwayLine", HALFWAY_LINE);
        fieldLines.put("CenterLine", CENTER_LINE);
        fieldLines.put("LeftPenaltyStretch", LEFT_PENALTY_STRETCH);
        fieldLines.put("RightPenaltyStretch", RIGHT_PENALTY_STRETCH);
        fieldLines.put("RightGoalTopLine", RIGHT_GOAL_TOP_LINE);
        fieldLines.put("RightGoalBottomLine", RIGHT_GOAL_BOTTOM_LINE);
        fieldLines.put("RightGoalDepthLine", RIGHT_GOAL_DEPTH_LINE);
        fieldLines.put("LeftGoalTopLine", LEFT_GOAL_TOP_LINE);
        fieldLines.put("LeftGoalBottomLine", LEFT_GOAL_BOTTOM_LINE);
        fieldLines.put("LeftGoalDepthLine", LEFT_GOAL_DEPTH_LINE);
        fieldLines.put("LeftFieldLeftPenaltyStretch", LEFT_FIELD_LEFT_PENALTY_STRETCH);
        fieldLines.put("LeftFieldRightPenaltyStretch", LEFT_FIELD_RIGHT_PENALTY_STRETCH);
        fieldLines.put("RightFieldLeftPenaltyStretch", RIGHT_FIELD_LEFT_PENALTY_STRETCH);
        fieldLines.put("RightFieldRightPenaltyStretch", RIGHT_FIELD_RIGHT_PENALTY_STRETCH);
    }

    private static final double penaltyClearanceRadius = 1414.00; // Ad Hoc for competition
    public static Circle2D[] getPenaltyCircles(double safetyOffset) {
        return new Circle2D[]{
                new Circle2D(new Vec2D(0, GvcGeometry.FIELD_LENGTH / 2), GvcGeometry.penaltyClearanceRadius + safetyOffset),
                new Circle2D(new Vec2D(0, -GvcGeometry.FIELD_LENGTH / 2), GvcGeometry.penaltyClearanceRadius + safetyOffset)
        };
    }

    public static Rect2D[] getPenaltyRegions(double saftyOffset) {
        Vec2D lpA = audienceToPlayer(LEFT_PENALTY_STRETCH.extend(saftyOffset).p1);
        Vec2D lpB = audienceToPlayer(LEFT_PENALTY_STRETCH.extend(saftyOffset).p2);
        Vec2D rpA = audienceToPlayer(RIGHT_PENALTY_STRETCH.extend(saftyOffset).p1);
        Vec2D rpB = audienceToPlayer(RIGHT_PENALTY_STRETCH.extend(saftyOffset).p2);

        if (lpA.x > lpB.x) {
            Vec2D tmp = lpA;
            lpA = lpB;
            lpB = tmp;
        }
        if (rpA.x > rpB.x) {
            Vec2D tmp = rpA;
            rpA = rpB;
            rpB = tmp;
        }
        double penaltyWidth = lpA.sub(lpB).mag();
        double penaltyHeight;
        if (lpA.y < 0) {
            penaltyHeight = (new Vec2D(lpA.x, -FIELD_LENGTH / 2)).sub(lpA).mag();
        } else {
            penaltyHeight = (new Vec2D(lpA.x, FIELD_LENGTH / 2)).sub(lpA).mag();
        }
        penaltyHeight += saftyOffset;

        if (lpA.y < rpA.y) {
            Vec2D lpC = new Vec2D(lpA.x, -FIELD_LENGTH / 2);
            return new Rect2D[]{new Rect2D(lpC, penaltyWidth, penaltyHeight),
                                new Rect2D(rpA, penaltyWidth, penaltyHeight)};
        } else {
            Vec2D rpC = new Vec2D(rpA.x, -FIELD_LENGTH / 2);
            return new Rect2D[]{new Rect2D(rpC, penaltyWidth, penaltyHeight),
                    new Rect2D(lpA, penaltyWidth, penaltyHeight)};
        }
    }

    public static Rect2D[] getPenaltyRegions() {
        Vec2D lpA = audienceToPlayer(LEFT_PENALTY_STRETCH.p1);
        Vec2D lpB = audienceToPlayer(LEFT_PENALTY_STRETCH.p2);
        Vec2D rpA = audienceToPlayer(RIGHT_PENALTY_STRETCH.p1);
        Vec2D rpB = audienceToPlayer(RIGHT_PENALTY_STRETCH.p2);

        if (lpA.x > lpB.x) {
            Vec2D tmp = lpA;
            lpA = lpB;
            lpB = tmp;
        }
        if (rpA.x > rpB.x) {
            Vec2D tmp = rpA;
            rpA = rpB;
            rpB = tmp;
        }
        double penaltyWidth = lpA.sub(lpB).mag();
        double penaltyHeight;
        if (lpA.y < 0) {
            penaltyHeight = (new Vec2D(lpA.x, -FIELD_LENGTH / 2)).sub(lpA).mag();
        } else {
            penaltyHeight = (new Vec2D(lpA.x, FIELD_LENGTH / 2)).sub(lpA).mag();
        }
        if (lpA.y < rpA.y) {
            Vec2D lpC = new Vec2D(lpA.x, -FIELD_LENGTH / 2);
            return new Rect2D[]{new Rect2D(lpC, penaltyWidth, penaltyHeight),
                    new Rect2D(rpA, penaltyWidth, penaltyHeight)};
        } else {
            Vec2D rpC = new Vec2D(rpA.x, -FIELD_LENGTH / 2);
            return new Rect2D[]{new Rect2D(rpC, penaltyWidth, penaltyHeight),
                    new Rect2D(lpA, penaltyWidth, penaltyHeight)};
        }
    }
}
