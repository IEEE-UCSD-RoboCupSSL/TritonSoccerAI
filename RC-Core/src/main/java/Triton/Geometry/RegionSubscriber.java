package Triton.Geometry;

import Triton.Detection.*;

public class RegionSubscriber implements Runnable {

    public void run() {
        while (true) {
            try {
                System.out.println("Blue 1 Region: " + 
                    Regions.getPartition(DetectionData.get().getRobotPos(Team.BLUE, 1)));
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}