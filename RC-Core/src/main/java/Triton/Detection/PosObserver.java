package Triton.Detection;

import Triton.DesignPattern.*;

public class PosObserver extends Observer {
    
    public PosObserver(Subject subject) {
        observe(subject);
    }

    @Override
    public void update(Subject subject) {
        if (subject instanceof DetectionManager)
            System.out.println("Position of B1: " + 
                ((DetectionManager) subject).getRobotPos(Team.BLUE, 1));
    }
}