package Triton.MoveTo;

import Triton.Shape.*;
import Triton.Detection.*;

import java.util.*;

public class MoveToPublisher implements Runnable {

    MoveToData moveto;

    public void run() {
        Scanner input = new Scanner(System.in);
        while (true) {
            // BLUE 1 4500 0
            System.out.println("Enter Move To: ");

            Team team = (input.next().equals("BLUE")) ? Team.BLUE : Team.YELLOW;
            int ID = input.nextInt(); 
            int desX = input.nextInt(); 
            int desY = input.nextInt(); 

            moveto = new MoveToData(team, ID, new Vec2D(desX, desY));
            MoveToData.publish(moveto);
        }
    }
}