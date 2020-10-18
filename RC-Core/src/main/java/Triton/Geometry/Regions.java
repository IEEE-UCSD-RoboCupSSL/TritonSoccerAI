package Triton.Geometry;

import Triton.Shape.*;
import Triton.DesignPattern.PubSubSystem.FieldSubscriber;
import Triton.DesignPattern.PubSubSystem.Subscriber;
import java.util.*;

import Proto.MessagesRobocupSslGeometry.*;

/*
 * Regions is definded as the collections of partitions of the field. 
 * Regions includes 9 parts: A, B, C, D, E, F, G, H, and I.
 */
public class Regions {
    private static HashMap<String, Shape2D> regions = new HashMap<String, Shape2D>();
    private static Subscriber<SSL_GeometryFieldSize> fieldSizeSub;
    private static Subscriber<HashMap<String, Line2D>> fieldLinesSub;

    public static void createRegions() {
        fieldSizeSub = new FieldSubscriber<SSL_GeometryFieldSize>("geometry", "fieldSize");
        fieldLinesSub = new FieldSubscriber<HashMap<String, Line2D>>("geometry", "fieldLines");
        while (!fieldSizeSub.subscribe() || !fieldLinesSub.subscribe());
         
        SSL_GeometryFieldSize fieldSize = fieldSizeSub.getMsg();
        HashMap<String, Line2D> fieldLines = fieldLinesSub.getMsg();

        // Create the center circle in the middle of the field
        double centerCircleRadius = fieldSize.getFieldArcs(0).getRadius();
        Circle2D centerCircle = new Circle2D(new Vec2D(0, 0), centerCircleRadius);
        Regions.addRegion("CentreCircle", centerCircle);

        double fieldWidth = fieldSize.getFieldLength();
        double fieldHeight = fieldSize.getFieldWidth();

        // Create the four quadtrants of the field
        Vec2D topLeftQuadAnchor = fieldLines.get("CenterLine").p1;
        Rect2D topLeftQuad = new Rect2D(topLeftQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("TopLeftQuad", topLeftQuad);

        Vec2D topRightQuadAnchor = fieldLines.get("CenterLine").midpoint();
        Rect2D topRightQuad = new Rect2D(topRightQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("TopRightQuad", topRightQuad);

        Vec2D bottomLeftQuadAnchor = fieldLines.get("LeftGoalLine").p1;
        Rect2D bottomLeftQuad = new Rect2D(bottomLeftQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("BottomLeftQuad", bottomLeftQuad);

        Vec2D bottomRightQuadAnchor = fieldLines.get("HalfwayLine").midpoint();
        Rect2D bottomRightQuad = new Rect2D(bottomRightQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("BottomRightQuad", bottomRightQuad);

        // Create fullfield
        Rect2D fullField = new Rect2D(bottomLeftQuadAnchor, fieldWidth, fieldHeight);
        Regions.addRegion("FullField", fullField);

        // Create two penalty rects
        double penaltyWidth = fieldLines.get("LeftPenaltyStretch").p1.x - fieldLines.get("LeftGoalLine").p1.x;
        double penaltyHeight = fieldLines.get("LeftPenaltyStretch").length();

        Vec2D leftPenaltyAnchor = new Vec2D(fieldLines.get("LeftGoalLine").p1.x,
                fieldLines.get("LeftPenaltyStretch").p1.y);
        Rect2D leftPenalty = new Rect2D(leftPenaltyAnchor, penaltyWidth, penaltyHeight);
        Regions.addRegion("LeftPenalty", leftPenalty);

        Vec2D rightPenaltyAnchor = fieldLines.get("RightPenaltyStretch").p1;
        Rect2D rightPenalty = new Rect2D(rightPenaltyAnchor, penaltyWidth, penaltyHeight);
        Regions.addRegion("RightPenalty", rightPenalty);
    }

    // Return the partition where the point is at.
    public static String getPartition(Vec2D point) {
        if (regions.get("FullField").isInside(point)) {
            if (regions.get("CentreCircle").isInside(point)) {
                return "A";
            }
            if (regions.get("TopLeftQuad").isInside(point)) {
                if (regions.get("LeftPenalty").isInside(point)) {
                    return "B";
                } else {
                    return "C";
                }
            } else if (regions.get("TopRightQuad").isInside(point)) {
                if (regions.get("RightPenalty").isInside(point)) {
                    return "E";
                } else {
                    return "D";
                }
            } else if (regions.get("BottomLeftQuad").isInside(point)) {
                if (regions.get("LeftPenalty").isInside(point)) {
                    return "F";
                } else {
                    return "G";
                }
            } else {
                if (regions.get("RightPenalty").isInside(point)) {
                    return "I";
                } else {
                    return "H";
                }
            }
        } else {
            return "X";
        }
    }

    public static void addRegion(String name, Shape2D region) {
        regions.put(name, region);
    }
}