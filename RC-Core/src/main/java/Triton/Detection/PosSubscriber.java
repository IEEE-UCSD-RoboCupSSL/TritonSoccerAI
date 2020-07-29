package Triton.Detection;

public class PosSubscriber implements Runnable {

    public void run() {
        while(true) {
            try {
                DetectionData detect = DetectionData.get();
                if (detect == null) {
                    System.out.println("Detection Null");
                } else {
                    System.out.println(detect.getRobotPos(Team.BLUE, 1));
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }
}