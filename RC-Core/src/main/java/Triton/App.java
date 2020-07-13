package Triton;

import Triton.Robot.VirtualBotConnection;
import Triton.Vision.*;
import Triton.Geometry.*;
import java.util.HashMap;

public class App 
{
    private static final String VISION_MULTICAST_ADDR = "224.5.23.3";
    private static final int VISION_PORT = 10020; 

    
    public static void main(String args[]) {

        //VirtualBotConnection.test();
        VisionConnection vision = new VisionConnection(VISION_MULTICAST_ADDR, VISION_PORT);
        FieldDetection fieldDetection = new FieldDetection();
        FieldGeometry fieldGeometry = new FieldGeometry();
        vision.addObserver(fieldDetection);
        vision.addObserver(fieldGeometry);

        vision.collectData();
        System.out.println("receiving");

        vision.preheating();
        
        HashMap<String, Line2D> lineSegments = fieldGeometry.getGeometry().field.lineSegments;

        // Create the center circle in the middle of the field
        Circle2D centerCircle = new Circle2D(new Point2D(0, 0), 
            fieldGeometry.getGeometry().field.centerCircleRadius);
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

        Point2D leftPenaltyAnchor = new Point2D(lineSegments.get("LeftGoalLine").p1.x, lineSegments.get("LeftPenaltyStretch").p1.y);
        Rect2D leftPenalty = new Rect2D(leftPenaltyAnchor, penaltyWidth, penaltyHeight);
        Regions.addRegion("LeftPenalty", leftPenalty);

        Point2D rightPenaltyAnchor = lineSegments.get("RightPenaltyStretch").p1;
        Rect2D rightPenalty = new Rect2D(rightPenaltyAnchor, penaltyWidth, penaltyHeight);
        Regions.addRegion("RightPenalty", rightPenalty);
        
        /*while(true) {
            vision.collectData();
            System.out.println(fieldDetection.getSentTime());
        }*/


        while(true) {
            vision.collectData();
            Point2D y1Loc = fieldDetection.getRobotLoc(false, 1); // Robot yellow 1
            System.out.println(Regions.getPartition(y1Loc));
        }
        /*
        
        final boolean BLUE = true, YELLOW = false;
        VisionConnection vision = new VisionConnection(VISION_MULTICAST_ADDR, VISION_PORT);
        FieldDetection fieldDetection = new FieldDetection();
        FieldGeometry fieldGeometry = new FieldGeometry();
        vision.addObserver(fieldDetection);
        vision.addObserver(fieldGeometry);

        System.out.println("***************************");

        
        vision.preheating();
        vision.collectData(1);
        System.out.println(fieldGeometry.geometry.toString());
        
        
        
        for(int i = 0; i > -1; i++) { 
            vision.collectData(4);
            // System.out.println(fieldDetection.toString());
            System.out.println(fieldDetection.getBallLoc()
                                 + " " + fieldDetection.getCaptureTime());
            
        }

        */
    }
}
