package Triton;

import Triton.Vision.*;


public class App 
{
    private static final String VISION_MULTICAST_ADDR = "224.5.23.3";
    private static final int VISION_PORT = 10002; 

    
    public static void main(String args[]) {
        
        final boolean BLUE = true, YELLOW = false;
        //Connection connection1 = new Connection(); // Where is this used?
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
            System.out.println(fieldDetection.getRobotLoc(BLUE, 0)
                                 + " " + fieldDetection.getCaptureTime());
            
        }
    }
}
