package Triton.Command;

import java.util.Scanner;

import Triton.Detection.Team;

public class CommandPublisher implements Runnable {

    CommandData commands = new CommandData();

    // taking in commands
    public void run() {
        Scanner sc = new Scanner(System.in);
        commands.publish();

        while (true) {
            // First letter determine the type of command
            // S: switch
            // M B1 [0,0] 10: Move B1 to [0,0] with 10 units per ms
            System.out.print("Please input command: ");
            
            String s = sc.nextLine();
            String[] commandString = s.split(" ");

            switch (commandString[0]) {
                case "S":
                    commands.add(new SwitchCommand());
                    break;
                case "M":
                    try {
                        String robot = commandString[1];
                        int ID = Character.getNumericValue(robot.charAt(1));
                        Team team = (robot.charAt(0) == 'B') ? Team.BLUE : Team.YELLOW;
                        String[] dest = (commandString[2]).split(",");
                        double x = Double.parseDouble(dest[0].replaceAll("[^\\d.-]", ""));
                        double y = Double.parseDouble(dest[1].replaceAll("[^\\d.-]", ""));
                        double speed = Double.parseDouble(commandString[3]);
    
                        commands.add(new MoveToCommand(team, ID, x, y, speed));
                    } catch (Exception e) {
                        System.out.println("Unparsable Input");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}