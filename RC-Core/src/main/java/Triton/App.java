package Triton;

import Triton.Detection.*;
import java.lang.Math;

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
        //FieldDetection fieldDetection = new FieldDetection();
        //FieldGeometry fieldGeometry = new FieldGeometry();
        //vision.addObserver(fieldDetection);
        //vision.addObserver(fieldGeometry);

        while(true) {
            vision.collectData();
            System.out.println(vision.dm.getRobot(Team.BLUE, 1).getVel());
            /*Point2D vel = vision.dm.getRobot(Team.BLUE, 1).getVel();
            if(Math.abs(vel.x) > 0.00001 || Math.abs(vel.y) > 0.00001) {
                System.out.println(vel);
            }*/
        }


        //System.out.println("receiving");

        //vision.preheating();
        //while () {} // TODO: better preheating ???
        
        //Regions.createRegions(fieldGeometry);
        
        /*while(true) {
            vision.collectData();
            System.out.println(fieldDetection.getSentTime());
        }*/

        /*Point2D preLoc = new Point2D(0, 0);
        double timestamp = 0.0;                   // TODO: calculate time between packages
        while(true) {
            vision.collectData();
            Point2D y1Loc = fieldDetection.getRobotLoc(true, 1); // Robot yellow 1
            double y1X = (y1Loc.x - preLoc.x) / timestamp;
            double y1y = (y1Loc.y - preLoc.y) / timestamp;
            Point2D y1Velocity = new Point2D(y1X, y1Y);
            preLoc = y1Loc;
            // TODO: add velocity to the robot
        }*/
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
