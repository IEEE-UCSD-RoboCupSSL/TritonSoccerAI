package Triton.Command;

import Triton.Detection.*;
import java.util.Scanner;

import Triton.ThreadManager.*;

public class CommandPublisher implements Runnable {

    private CommandData commands = new CommandData();
    ThreadManager threadManager;

    public CommandPublisher() {
        commands.publish();
    }

    // taking in commands
    public void run() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            // First letter determine the type of command
            // S: switch
            // M B1 [0,0] 10: Move B1 to [0,0] with 10 units per ms
            String s = sc.nextLine();
            String[] commandString = s.split(" ");

            switch (commandString[0]) {
                case "S":
                    commands.add(new SwitchCommand()); 
                    break;
                case "M":
                    String robot = commandString[1]; //"B1"
                    Team team;
                    if (robot.substring(0, 1).equals("B")) {
                        team = Team.BLUE;
                    }
                    else {
                        team = Team.YELLOW;
                    }
                    int num = (int) commandString[1].charAt(1);

                    String[] pos = commandString[2].split(",");
                    double posX = Double.parseDouble(pos[0]);
                    double posY = Double.parseDouble(pos[1]);

                    int speed = Integer.parseInt(commandString[3]);
                    commands.add(new MoveToCommand(team, num, posX, posY, speed));
                default:
                    break;
            }
        }
    }
}