package Triton.Detection;

public class PosSubscriber implements Runnable {

    public void run() {
        while(true) {
            try {
                System.out.println("Blue 1 Position: " +
                    DetectionData.get().getRobotPos(Team.BLUE, 1));
            } catch (Exception e) {
                // do nothing
            }
        }
    }
}