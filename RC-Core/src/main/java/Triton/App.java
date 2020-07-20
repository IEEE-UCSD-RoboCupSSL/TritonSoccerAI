package Triton;

import Triton.Vision.*;
import Triton.Geometry.*;
import Triton.Detection.*;

public class App 
{
    private static final String VISION_MULTICAST_ADDR = "224.5.23.3";
    private static final int VISION_PORT = 10020; 
    
    public static void main(String args[]) {

        VisionConnection vision = new VisionConnection(VISION_MULTICAST_ADDR, VISION_PORT);

        VelObserver vo = new VelObserver(vision.dm);
        PosObserver po = new PosObserver(vision.dm);
        
        while(true) {
            vision.collectData();
            //if(vision.geoInit) {
            //    System.out.println(Regions.getPartition(vision.dm.getBallPos()));
            //}
        }
    }
}
