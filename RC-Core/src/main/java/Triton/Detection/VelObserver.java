package Triton.Detection;

import Triton.DesignPattern.*;

public class VelObserver extends Observer {
    
    public VelObserver(Subject subject) {
        observe(subject);
    }

    @Override
    public void update(Subject subject) {
        if (subject instanceof DetectionManager)
            System.out.println("Velocity of B1: " + 
                ((DetectionManager) subject).getRobot(Team.BLUE, 1).getVel());
    }
}