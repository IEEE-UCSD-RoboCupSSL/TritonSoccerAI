package Triton.Vision;

import Triton.Geometry.*;
import java.util.HashMap;

/*
 * Regions is definded as the collections of partitions of the field. 
 * Regions includes 9 parts: A, B, C, D, E, F, G, H, and I.
 */
public class Regions {
    private static HashMap<String, Shape2D> regions = new HashMap<String, Shape2D>();

    public static void createRegions(FieldGeometry fieldGeometry) {
        HashMap<String, Line2D> lineSegments = fieldGeometry.getGeometry().field.lineSegments;

        // Create the center circle in the middle of the field
        Circle2D centerCircle = new Circle2D(new Point2D(0, 0), fieldGeometry.getGeometry().field.centerCircleRadius);
        Regions.addRegion("CentreCircle", centerCircle);

        double fieldWidth = lineSegments.get("CenterLine").length();
        double fieldHeight = lineSegments.get("HalfwayLine").length();

        // Create the four quadtrants of the field
        Point2D topLeftQuadAnchor = lineSegments.get("CenterLine").p1;
        Rect2D topLeftQuad = new Rect2D(topLeftQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("TopLeftQuad", topLeftQuad);

        Point2D topRightQuadAnchor = lineSegments.get("CenterLine").midpoint();
        Rect2D topRightQuad = new Rect2D(topRightQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("TopRightQuad", topRightQuad);

        Point2D bottomLeftQuadAnchor = lineSegments.get("LeftGoalLine").p1;
        Rect2D bottomLeftQuad = new Rect2D(bottomLeftQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("BottomLeftQuad", bottomLeftQuad);

        Point2D bottomRightQuadAnchor = lineSegments.get("HalfwayLine").midpoint();
        Rect2D bottomRightQuad = new Rect2D(bottomRightQuadAnchor, fieldWidth / 2, fieldHeight / 2);
        Regions.addRegion("BottomRightQuad", bottomRightQuad);

        // Create fullfield
        Rect2D fullField = new Rect2D(bottomLeftQuadAnchor, fieldWidth, fieldHeight);
        Regions.addRegion("FullField", fullField);

        // Create two penalty rects
        double penaltyWidth = lineSegments.get("LeftPenaltyStretch").p1.x - lineSegments.get("LeftGoalLine").p1.x;
        double penaltyHeight = lineSegments.get("LeftPenaltyStretch").length();

        Point2D leftPenaltyAnchor = new Point2D(lineSegments.get("LeftGoalLine").p1.x,
                lineSegments.get("LeftPenaltyStretch").p1.y);
        Rect2D leftPenalty = new Rect2D(leftPenaltyAnchor, penaltyWidth, penaltyHeight);
        Regions.addRegion("LeftPenalty", leftPenalty);

        Point2D rightPenaltyAnchor = lineSegments.get("RightPenaltyStretch").p1;
        Rect2D rightPenalty = new Rect2D(rightPenaltyAnchor, penaltyWidth, penaltyHeight);
        Regions.addRegion("RightPenalty", rightPenalty);
    }

    // Return the partition where the point is at.
    public static String getPartition(Point2D point) {
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