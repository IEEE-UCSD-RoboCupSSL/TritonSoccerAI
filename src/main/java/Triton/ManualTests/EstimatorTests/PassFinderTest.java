package Triton.ManualTests.EstimatorTests;

import Triton.CoreModules.AI.Estimators.PassFinder;
import Triton.CoreModules.Ball.Ball;
import Triton.CoreModules.Robot.Ally.Ally;
import Triton.CoreModules.Robot.Foe.Foe;
import Triton.CoreModules.Robot.RobotList;
import Triton.PeriphModules.Display.Display;
import Triton.PeriphModules.Display.PaintOption;

import java.util.ArrayList;
import java.util.Scanner;

import static Triton.PeriphModules.Display.PaintOption.*;
import static Triton.PeriphModules.Display.PaintOption.PROBABILITY;

public class PassFinderTest {

    PassFinder passFinder;
    Scanner scanner;

    public PassFinderTest(Scanner scanner, RobotList<Ally> fielders, RobotList<Foe> foes, Ball ball) {
        this.scanner = scanner;
        passFinder = new PassFinder(fielders, foes, ball);
        passFinder.run();
    }

    public boolean test() {
        Display display = new Display();
        ArrayList<PaintOption> paintOptions = new ArrayList<>();
        paintOptions.add(GEOMETRY);
        paintOptions.add(OBJECTS);
        paintOptions.add(INFO);
        paintOptions.add(PROBABILITY);
        display.setPaintOptions(paintOptions);
        display.setProbFinder(passFinder);

        while(true) {
            System.out.println(">> ENTER CANDIDATE:");
            int candidate;
            try {
                candidate = scanner.nextInt();
            } catch(Exception e) {
                System.out.println(">> QUIT");
                return true;
            }
            scanner.nextLine();
            passFinder.fixCandidate(candidate);
        }
    }

}
