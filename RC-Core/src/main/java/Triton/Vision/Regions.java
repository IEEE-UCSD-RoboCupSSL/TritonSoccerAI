package Triton.Vision;

import Triton.Geometry.*;
import java.util.HashMap;
/*
 * Regions is definded as the collections of partitions of the field. 
 * Regions includes 9 parts: A, B, C, D, E, F, G, H, and I.
 */
public class Regions {
    private static HashMap<String, Shape2D> regions = new HashMap<String, Shape2D>();
    
    // Return the partition where the point is at.
    public static String getPartition(Point2D point) {
        if(regions.get("FullField").isInside(point)) {
            if(regions.get("CentreCircle").isInside(point)) {
                return "A";
            }
            if(regions.get("TopLeftQuad").isInside(point)) {
                if(regions.get("LeftPenalty").isInside(point)) {
                    return "B";
                } else {
                    return "C";
                }
            } else if(regions.get("TopRightQuad").isInside(point)) {
                if(regions.get("RightPenalty").isInside(point)) {
                    return "E";
                } else {
                    return "D";
                }
            } else if(regions.get("BottomLeftQuad").isInside(point)) {
                if(regions.get("LeftPenalty").isInside(point)) {
                    return "F";
                } else {
                    return "G";
                }
            } else {
                if(regions.get("RightPenalty").isInside(point)) {
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